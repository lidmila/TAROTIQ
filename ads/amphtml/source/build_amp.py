# Builds AMPHTML (amp4ads) ad creatives for Google Ads HTML5 upload.
# AMPHTML satisfies the HTML5-upload eligibility criterion without the $9k spend gate.
# Spec: https://amp.dev/documentation/guides-and-tutorials/learn/a4a_spec/
#  - <html amp4ads> boilerplate + amp4ads-v0.js runtime
#  - no custom JS; <amp-img> instead of <img>
#  - keyframes/transitions may animate ONLY opacity and transform (ours do)
#  - CSS <= 20,000 bytes; no position:fixed/sticky
import os, shutil, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
SB = r"C:/Users/lidmi/AppData/Local/Temp/claude/C--Users-lidmi-Documents-GitHub-TAROTIQ/46b3b937-38d0-4aaf-9fd6-ed74993d137c/scratchpad"
AST = SB + "/safe/assets"   # 300px palette PNGs
DEST = r"C:/Users/lidmi/Documents/GitHub/TAROTIQ/ads/amphtml/zips_upload"
WORK = HERE + "/work"
CLICK = "https://play.google.com/store/apps/details?id=com.tarotiq.app"

HEAD = """<!doctype html>
<html amp4ads>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1">
<meta name="ad.size" content="width={W},height={H}">
<style amp4ads-boilerplate>body{{visibility:hidden}}</style>
<script async src="https://cdn.ampproject.org/amp4ads-v0.js"></script>
<style amp-custom>
{CSS}
</style>
</head>
<body>
{BODY}
</body>
</html>
"""

CSS_COMMON = """body{margin:0;padding:0;font-family:Georgia,'Times New Roman',serif;background:#0C0A08}
.ad{position:relative;overflow:hidden}
.wrap{position:absolute;top:0;right:0;bottom:0;left:0;display:flex}
.logo{color:#D4AF37;font-weight:bold}
.head{color:#F5ECD7}
.head b{color:#E8C84A}
.rev{color:#BFAE8E;font-style:italic;opacity:0;animation:fade .6s ease 1.2s forwards}
.cta{color:#1A140C;font-weight:bold;white-space:nowrap;
  background:linear-gradient(135deg,#E8C84A,#C4963A 60%,#A08520);
  box-shadow:0 0 16px rgba(232,200,74,.4);animation:pz 1.6s ease-in-out 1.4s infinite}
.play{color:#8A7A60;letter-spacing:1px}
.card{position:relative;overflow:hidden;border:1px solid #E8C84A;
  box-shadow:0 0 12px rgba(232,200,74,.45),0 6px 14px rgba(0,0,0,.5);
  opacity:0;transform:translateY(14px);animation:up .6s ease forwards}
amp-img{display:block}
@keyframes up{to{opacity:1;transform:translateY(0)}}
@keyframes pop{to{opacity:1;transform:scale(1)}}
@keyframes fade{to{opacity:1}}
@keyframes pz{0%,100%{transform:scale(1)}50%{transform:scale(1.05)}}
"""

# ---------- three-card ----------
TC_CARDS = """      <div class="row">
        <div class="card c1"><amp-img src="moon.png" layout="fill" alt=""></amp-img></div>
        <div class="card c2"><amp-img src="star.png" layout="fill" alt=""></amp-img></div>
        <div class="card c3"><amp-img src="sun.png" layout="fill" alt=""></amp-img></div>
      </div>"""

TC_PORT_CSS = CSS_COMMON + """.ad{width:320px;height:480px;background:radial-gradient(120% 90% at 50% 0%,#241B14 0%,#16110C 48%,#0C0A08 100%)}
.wrap{flex-direction:column;align-items:center;justify-content:space-between;padding:26px 18px 22px}
.logo{letter-spacing:4px;font-size:15px}
.head{font-size:20px;text-align:center;margin-top:6px}
.mid{display:flex;flex-direction:column;align-items:center}
.row{display:flex;justify-content:center;gap:9px}
.card{width:80px;height:132px;border-radius:6px}
.c1{animation-delay:.2s}.c2{animation-delay:.5s}.c3{animation-delay:.8s}
.labels{display:flex;justify-content:center;gap:9px;margin-top:5px}
.labels span{width:80px;text-align:center;color:#BFAE8E;font-size:9px;letter-spacing:1px}
.rev{font-size:14px;text-align:center;margin-top:8px}
.cta{padding:11px 26px;border-radius:26px;font-size:15px}
.play{font-size:11px}
"""

TC_PORT_BODY = """<div class="ad">
  <div class="wrap">
    <div class="logo">TAROTIQ</div>
    <div class="head">Past &middot; Present &middot; <b>Future</b></div>
    <div class="mid">
""" + TC_CARDS + """
      <div class="labels"><span>PAST</span><span>PRESENT</span><span>FUTURE</span></div>
      <div class="rev">Your story, revealed by AI</div>
    </div>
    <div class="cta">Get your free reading</div>
    <div class="play">Personal AI tarot &middot; Free to try</div>
  </div>
</div>"""

TC_LAND_CSS = CSS_COMMON + """.ad{width:480px;height:320px;background:radial-gradient(120% 120% at 20% 0%,#241B14 0%,#16110C 50%,#0C0A08 100%)}
.wrap{flex-direction:row;align-items:center;justify-content:space-between;padding:0 30px}
.txt{display:flex;flex-direction:column;align-items:flex-start;gap:12px;width:210px}
.logo{letter-spacing:4px;font-size:16px}
.head{font-size:23px;line-height:1.15}
.rev{font-size:13px}
.cta{padding:11px 24px;border-radius:26px;font-size:15px}
.row{display:flex;gap:8px}
.card{width:66px;height:109px;border-radius:6px}
.c1{animation-delay:.2s}.c2{animation-delay:.5s}.c3{animation-delay:.8s}
.play{font-size:11px}
"""

TC_LAND_BODY = """<div class="ad">
  <div class="wrap">
    <div class="txt">
      <div class="logo">TAROTIQ</div>
      <div class="head">Past &middot; Present<br>&middot; <b>Future</b></div>
      <div class="rev">Your story, revealed by AI</div>
      <div class="cta">Get your free reading</div>
      <div class="play">Personal AI tarot &middot; Free to try</div>
    </div>
""" + TC_CARDS + """
  </div>
</div>"""

# ---------- single card ----------
SG_PORT_CSS = CSS_COMMON + """.ad{width:320px;height:480px;background:radial-gradient(120% 90% at 50% 0%,#241B14 0%,#16110C 48%,#0C0A08 100%)}
.wrap{flex-direction:column;align-items:center;justify-content:space-between;padding:24px 18px 20px}
.logo{letter-spacing:4px;font-size:15px}
.head{font-size:21px;text-align:center;margin-top:4px}
.mid{display:flex;flex-direction:column;align-items:center}
.cardwrap{position:relative;width:152px;height:250px;margin-bottom:6px}
.glow{position:absolute;left:50%;top:50%;width:210px;height:290px;transform:translate(-50%,-50%);
  background:radial-gradient(ellipse at center,rgba(232,200,74,.30),transparent 70%)}
.card{width:152px;height:250px;border-radius:8px;transform:scale(.94);animation:pop .7s ease .2s forwards}
.nm{color:#E8C84A;letter-spacing:2px;font-size:18px;text-align:center;margin-top:4px}
.rev{font-size:13px;text-align:center;margin-top:4px}
.cta{padding:11px 26px;border-radius:26px;font-size:15px}
.play{font-size:11px}
"""

SG_PORT_BODY = """<div class="ad">
  <div class="wrap">
    <div class="logo">TAROTIQ</div>
    <div class="head">{HEAD}</div>
    <div class="mid">
      <div class="cardwrap"><div class="glow"></div><div class="card"><amp-img src="{IMG}" layout="fill" alt=""></amp-img></div></div>
      <div class="nm">{NAME}</div>
      <div class="rev">{REVEAL}</div>
    </div>
    <div class="cta">{CTA}</div>
    <div class="play">Personal AI tarot &middot; Free to try</div>
  </div>
</div>"""

SG_LAND_CSS = CSS_COMMON + """.ad{width:480px;height:320px;background:radial-gradient(120% 120% at 20% 0%,#241B14 0%,#16110C 50%,#0C0A08 100%)}
.wrap{flex-direction:row;align-items:center;justify-content:space-between;padding:0 32px}
.txt{display:flex;flex-direction:column;align-items:flex-start;gap:12px;width:224px}
.logo{letter-spacing:4px;font-size:16px}
.head{font-size:24px;line-height:1.12}
.rev{font-size:13px}
.cta{padding:11px 24px;border-radius:26px;font-size:15px}
.cardwrap{position:relative;width:158px;height:260px}
.glow{position:absolute;left:50%;top:50%;width:210px;height:300px;transform:translate(-50%,-50%);
  background:radial-gradient(ellipse at center,rgba(232,200,74,.30),transparent 70%)}
.card{width:158px;height:260px;border-radius:8px;transform:scale(.94);animation:pop .7s ease .2s forwards}
.play{font-size:11px}
"""

SG_LAND_BODY = """<div class="ad">
  <div class="wrap">
    <div class="txt">
      <div class="logo">TAROTIQ</div>
      <div class="head">{HEAD}</div>
      <div class="rev">{REVEAL}</div>
      <div class="cta">{CTA}</div>
      <div class="play">Personal AI tarot &middot; Free to try</div>
    </div>
    <div class="cardwrap"><div class="glow"></div><div class="card"><amp-img src="{IMG}" layout="fill" alt=""></amp-img></div></div>
  </div>
</div>"""

REL = dict(IMG="lovers.png", NAME="THE LOVERS", HEAD="Where is your <b>love heading?</b>",
           REVEAL="Your 5-card love reading", CTA="Get your free reading")
DAY = dict(IMG="sun.png", NAME="THE SUN", HEAD="Your card for <b>today</b>",
           REVEAL="A fresh reading, every day", CTA="Draw your card")

def emit(name, w, h, css, body, assets, content=None):
    if content:
        for k, v in content.items():
            body = body.replace("{"+k+"}", v)
    html = HEAD.format(W=w, H=h, CSS=css.strip(), BODY=body, CLICK=CLICK)
    d = os.path.join(WORK, name); os.makedirs(d)
    open(os.path.join(d, "index.html"), "w", encoding="utf-8").write(html)
    for a in assets:
        shutil.copyfile(os.path.join(AST, a), os.path.join(d, a))
    zp = os.path.join(DEST, name + ".zip")
    if os.path.exists(zp): os.remove(zp)
    with zipfile.ZipFile(zp, "w", zipfile.ZIP_DEFLATED) as z:
        for f in sorted(os.listdir(d)):
            z.write(os.path.join(d, f), arcname=f)
    names = zipfile.ZipFile(zp).namelist()
    assert not any('/' in n or chr(92) in n for n in names), names
    print("  %-34s %6.1f KB  css=%d B" % (name+".zip", os.path.getsize(zp)/1024, len(css.encode())))

shutil.rmtree(WORK, ignore_errors=True); os.makedirs(WORK)
os.makedirs(DEST, exist_ok=True)
for f in list(os.listdir(DEST)):
    if f.endswith(".zip"): os.remove(os.path.join(DEST, f))

TCA = ["moon.png", "star.png", "sun.png"]
print("threecard:")
emit("threecard_portrait", 320, 480, TC_PORT_CSS, TC_PORT_BODY, TCA)
emit("threecard_landscape", 480, 320, TC_LAND_CSS, TC_LAND_BODY, TCA)
print("relationship:")
emit("relationship_portrait", 320, 480, SG_PORT_CSS, SG_PORT_BODY, ["lovers.png"], REL)
emit("relationship_landscape", 480, 320, SG_LAND_CSS, SG_LAND_BODY, ["lovers.png"], REL)
print("dailycard:")
emit("dailycard_portrait", 320, 480, SG_PORT_CSS, SG_PORT_BODY, ["sun.png"], DAY)
emit("dailycard_landscape", 480, 320, SG_LAND_CSS, SG_LAND_BODY, ["sun.png"], DAY)
print("DONE ->", DEST)
