import re

file_path = '/Users/ghazur_it/Documents/khanza-paling-baru/SIMRS-Khanza-master/report/rptDaftarResepPulang.jrxml'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Page settings
content = content.replace('pageWidth="842"', 'pageWidth="595"')
content = content.replace('pageHeight="595"', 'pageHeight="842"')
content = content.replace('orientation="Landscape"', 'orientation="Portrait"')
content = content.replace('columnWidth="802"', 'columnWidth="555"')

# 2. Lines width
content = content.replace('width="802"', 'width="555"')
content = content.replace('width="800"', 'width="553"')

# 3. Title widths and positions
# name, alamat, etc.
content = content.replace('x="50" y="0" width="702"', 'x="50" y="0" width="455"')
content = content.replace('x="50" y="14" width="702"', 'x="50" y="14" width="455"')
content = content.replace('x="50" y="35" width="702"', 'x="50" y="35" width="455"')
content = content.replace('x="50" y="25" width="702"', 'x="50" y="25" width="455"')
content = content.replace('x="754"', 'x="507"')

# 4. Column Header and Detail bounds
def replace_bounds(content, old_x, old_w, new_x, new_w):
    # Header bounds
    content = re.sub(f'x="{old_x}" y="0" width="{old_w}"', f'x="{new_x}" y="0" width="{new_w}"', content)
    return content

# No.Permintaan (temp1): x=0, w=60
content = replace_bounds(content, "0", "66", "0", "60")
# Tanggal (temp2): x=66, w=82 -> x=60, w=70
content = replace_bounds(content, "66", "82", "60", "70")
# Ruang/Kamar (temp3): x=148, w=129 -> x=130, w=100
content = replace_bounds(content, "148", "129", "130", "100")
# Status (temp4): x=277, w=83 -> x=230, w=60
content = replace_bounds(content, "277", "83", "230", "60")
# Pasien (temp5): x=360, w=250 -> x=290, w=150
content = replace_bounds(content, "360", "250", "290", "150")
# Dokter Yang Meminta (temp6): x=610, w=192 -> x=440, w=115
content = replace_bounds(content, "610", "192", "440", "115")

# 5. Footer bounds
# "Page " + $V{PAGE_NUMBER} + " of "
content = content.replace('x="247" y="6" width="257"', 'x="150" y="6" width="257"')
# "" + $V{PAGE_NUMBER}
content = content.replace('x="504" y="6" width="36"', 'x="407" y="6" width="36"')


with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done modifying.")
