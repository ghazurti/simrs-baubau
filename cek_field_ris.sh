#!/bin/zsh
# Cek struktur respons detail worklist RIS alat_rs (field Satu Sehat yang dikembalikan RIS).
# Jalankan dari root repo saat terhubung ke jaringan RS:
#   ./cek_field_ris.sh 512 511
# (angka = worklist_id_alatrs dari tabel bridging_alatrs_worklist yang status_hasil='Sudah')
# Hanya nama field + nilai field id/status yang ditampilkan; data pasien disamarkan.
cd "$(dirname "$0")"
P=$(mktemp -d)
trap 'rm -rf "$P"' EXIT
CP="dist/lib/commons-codec-1.12.jar:build/classes:KhanzaSecurity16bit/build/classes:dist/lib/*"
cat > "$P/CetakLoginRIS.java" <<'EOF'
import fungsi.koneksiDB;
public class CetakLoginRIS {
    public static void main(String[] a) {
        String url = koneksiDB.URLALATRS(), port = koneksiDB.PORTALATRS();
        System.out.println(port == null || port.isEmpty() ? url : url + ":" + port);
        System.out.println("{\"username\":\"" + koneksiDB.USERALATRS() + "\",\"password\":\"" + koneksiDB.PASSALATRS() + "\"}");
    }
}
EOF
javac -nowarn -cp "$CP" -d "$P" "$P/CetakLoginRIS.java" 2>/dev/null || { echo "Gagal compile helper"; exit 1; }
java -cp "$P:$CP" CetakLoginRIS > "$P/login" 2>/dev/null
B=$(sed -n 1p "$P/login")
echo "RIS: $B"
TOK=$(sed -n 2p "$P/login" | curl -s -m 15 -H 'Content-Type: application/json' -d @- "$B/api/user/login" \
      | python3 -I -c 'import sys,json; print(json.load(sys.stdin)["data"][0]["token"])' 2>/dev/null)
[ -n "$TOK" ] || { echo "Login RIS gagal (cek jaringan RS / setting URLALATRS)."; exit 1; }
for id in "$@"; do
  curl -s -m 20 -H "Authorization: Bearer $TOK" "$B/api/worklist/details_by_id/$id" > "$P/w$id.json"
done
python3 -I - "$P"/w*.json <<'EOF'
import json, sys
kunci = ("satu", "sehat", "ihs", "imaging", "report", "request", "encounter",
         "observation", "accession", "status", "_id", "uuid")
def jalan(n, p=""):
    if isinstance(n, dict):
        for k, v in n.items():
            jalan(v, f"{p}.{k}" if p else k)
    elif isinstance(n, list):
        print(f"{p} [list {len(n)}]")
        if n:
            jalan(n[0], p + "[0]")
    else:
        k = p.lower(); v = "" if n is None else str(n)
        tampil = any(x in k for x in kunci) or k.endswith("id")
        print(f"{p} = {v[:80] if tampil else ('(kosong)' if v == '' else '(ada isi)')}")
for f in sys.argv[1:]:
    print("=====", f.rsplit("/", 1)[1])
    try:
        jalan(json.load(open(f)))
    except Exception as e:
        print("(respons bukan JSON / kosong)", e)
EOF
