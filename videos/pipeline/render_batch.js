const { chromium } = require('playwright');
const { execFileSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// Config
const W = 1080, H = 1920, DUR = 8000, FPS = 60, SS = 1; // native res; JPEG capture is fast
const DT = 1000 / FPS;
const TOTAL = Math.round(DUR * FPS / 1000);
const OUTDIR = path.join(__dirname, 'out');
fs.mkdirSync(OUTDIR, { recursive: true });

const ALL = ['star', 'moon', 'sun', 'priestess', 'lovers', 'fortune'];
const variants = (process.argv[2] ? process.argv[2].split(',') : ALL);
const GPU = ['--use-gl=angle','--use-angle=d3d11','--enable-gpu','--ignore-gpu-blocklist',
             '--force-color-profile=srgb','--hide-scrollbars'];

(async () => {
  const browser = await chromium.launch({ args: GPU });
  const context = await browser.newContext({ viewport: { width: W, height: H }, deviceScaleFactor: SS });
  const page = await context.newPage();

  for (const v of variants) {
    const framesDir = path.join(__dirname, 'frames');
    fs.rmSync(framesDir, { recursive: true, force: true });
    fs.mkdirSync(framesDir, { recursive: true });

    const url = 'file://' + path.resolve(__dirname, 'template.html').replace(/\\/g, '/') + '?v=' + v;
    await page.goto(url);
    await page.evaluate(() => window.__ready);
    await page.waitForTimeout(300);

    const t0 = Date.now();
    for (let i = 0; i < TOTAL; i++) {
      await page.evaluate((tt) => window.seek(tt), i * DT);
      await page.screenshot({ path: path.join(framesDir, 'f' + String(i).padStart(4, '0') + '.jpg'),
        type: 'jpeg', quality: 100, clip: { x: 0, y: 0, width: W, height: H } });
    }
    console.log(`[${v}] captured ${TOTAL} frames in ${((Date.now()-t0)/1000).toFixed(1)}s`);

    const out = path.join(OUTDIR, 'tarotiq_' + v + '.mp4');
    // deband smooths any residual gradient banding; lanczos scale is a no-op at SS=1 but supports SS>1
    execFileSync('ffmpeg', [
      '-y', '-framerate', String(FPS),
      '-i', path.join(framesDir, 'f%04d.jpg'),
      '-vf', `scale=${W}:${H}:flags=lanczos,deband=range=22:blur=false,format=yuv420p`,
      '-c:v', 'libx264', '-preset', 'slow', '-crf', '16',
      '-movflags', '+faststart', '-r', String(FPS), out
    ], { stdio: 'ignore' });
    console.log(`[${v}] encoded -> ${out}`);
  }

  await browser.close();
  console.log('ALL DONE');
})().catch(e => { console.error(e); process.exit(1); });
