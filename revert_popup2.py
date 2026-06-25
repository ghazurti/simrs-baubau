import re

with open("/Users/ghazur_it/Documents/khanza-paling-baru/SIMRS-Khanza-master/src/simrskhanza/DlgReg.java", "r") as f:
    content = f.read()

# remove popup add
content = content.replace('        MenuInputData.add(ppBerkasDiterima);\n', '')
content = content.replace('        MenuInputData1.add(ppBerkasDiterima1);\n', '')

with open("/Users/ghazur_it/Documents/khanza-paling-baru/SIMRS-Khanza-master/src/simrskhanza/DlgReg.java", "w") as f:
    f.write(content)

print("Popup removed 2")
