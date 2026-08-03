import os, shutil, zipfile
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
SB = r"C:/Users/lidmi/AppData/Local/Temp/claude/C--Users-lidmi-Documents-GitHub-TAROTIQ/46b3b937-38d0-4aaf-9fd6-ed74993d137c/scratchpad"
HD = SB + "/video/hd"
DEST = r"C:/Users/lidmi/Documents/GitHub/TAROTIQ/ads/html5_appcampaign/zips_upload"
AST = HERE + "/assets"; os.makedirs(AST, exist_ok=True)
WORK = HERE + "/work"; shutil.rmtree(WORK, ignore_errors=True); os.makedirs(WORK)

SRC = {'card_back':HD+'/card_back.png','moon':HD+'/the_moon.jpg','star':HD+'/the_star.jpg','sun':HD+'/the_sun.jpg','lovers':HD+'/the_lovers.jpg'}
for name, p in SRC.items():
    im = Image.open(p).convert('RGB'); im = im.resize((300, int(im.height*300/im.width)), Image.LANCZOS)
    im.convert('P', palette=Image.ADAPTIVE, colors=256).save(AST+"/"+name+".png", 'PNG', optimize=True)

# wipe old zips in DEST
os.makedirs(DEST, exist_ok=True)
for f in os.listdir(DEST):
    fp = os.path.join(DEST, f)
    if os.path.isdir(fp): shutil.rmtree(fp)
    elif f.endswith('.zip'): os.remove(fp)

def emit(out_name, template, assets, content=None):
    html = open(HERE+"/"+template, encoding="utf-8").read()
    if content:
        for k,v in content.items(): html = html.replace("{{"+k+"}}", v)
    d = os.path.join(WORK, out_name); os.makedirs(d)
    open(os.path.join(d,"index.html"),"w",encoding="utf-8").write(html)
    for a in assets: shutil.copyfile(AST+"/"+a, os.path.join(d,a))
    zp = os.path.join(DEST, out_name+".zip")
    with zipfile.ZipFile(zp,"w",zipfile.ZIP_DEFLATED) as z:
        for f in sorted(os.listdir(d)): z.write(os.path.join(d,f), arcname=f)
    names = zipfile.ZipFile(zp).namelist()
    assert not any('/' in n or chr(92) in n for n in names), names
    print("  %-30s %5.1f KB  %s" % (out_name+".zip", os.path.getsize(zp)/1024, names))

REL = dict(IMG="lovers.png", NAME="THE LOVERS", HEAD="Where is your <b>love heading?</b>",
           REVEAL="Your 5-card love reading", CTA="Get your free reading", CTA_LAND="Read my love")
DAY = dict(IMG="sun.png", NAME="THE SUN", HEAD="Your card for <b>today</b>",
           REVEAL="A fresh reading, every day", CTA="Draw your card", CTA_LAND="Draw your card")

print("three_card:")
emit("threecard_portrait", "tc_port.html", ["moon.png","star.png","sun.png"])
emit("threecard_landscape","tc_land.html", ["moon.png","star.png","sun.png"])
print("relationship:")
emit("relationship_portrait","sg_port.html", ["lovers.png"], REL)
emit("relationship_landscape","sg_land.html", ["lovers.png"], REL)
print("dailycard:")
emit("dailycard_portrait","sg_port.html", ["sun.png"], DAY)
emit("dailycard_landscape","sg_land.html", ["sun.png"], DAY)
print("DONE ->", DEST)
