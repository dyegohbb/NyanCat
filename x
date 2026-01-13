Usei como base o documento “Secure File Upload in PHP Web Applications”, que deixa claro que não dá pra confiar em MIME, extensão nem conteúdo isoladamente, e que a única forma decente é whitelist + inspeção real do arquivo + não confiar no nome + bloquear qualquer coisa que não seja exatamente o esperado (ver principalmente páginas 5 a 13, onde o autor mostra como MIME e extensão são burláveis). 

Abaixo vai um index.html completo, só front, com JS dentro, focado em XML, aplicando exatamente esses princípios no lado do cliente para reduzir superfície de ataque e ruído de SAST (sabendo que segurança real continua no backend).

Como você pediu: sem PHP, index.html fechado, pronto pra colar.

<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Upload Seguro de XML</title>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>

<h2>Upload de XML</h2>

<form action="/outroprojeto/upload" method="post" enctype="multipart/form-data" target="result">
  <label>Arquivo XML:</label><br>
  <input type="file" name="arquivo" accept=".xml,application/xml,text/xml" required><br><br>

  <label>CTRL:</label><br>
  <input type="text" name="CTRL" required><br><br>

  <button type="submit">Enviar</button>
</form>

<br>
<iframe id="result" name="result" width="700" height="200"></iframe>

<script>
(() => {
  const MAX_BYTES = 2 * 1024 * 1024
  const ALLOWED_MIME = new Set(["application/xml","text/xml",""])

  const getExt = n => {
    const i = n.lastIndexOf(".")
    return i >= 0 ? n.slice(i+1).toLowerCase() : ""
  }

  const looksBinary = buf => {
    const b = new Uint8Array(buf)
    if (!b.length) return false
    let suspicious = 0
    const n = Math.min(b.length, 4096)
    for (let i = 0; i < n; i++) {
      const x = b[i]
      if (x === 0) return true
      if (x < 9 || (x > 13 && x < 32)) suspicious++
    }
    return (suspicious / n) > 0.03
  }

  const readText = f => new Promise((r,j)=>{
    const fr = new FileReader()
    fr.onerror = () => j()
    fr.onload = () => r(String(fr.result || ""))
    fr.readAsText(f)
  })

  const readBuffer = f => new Promise((r,j)=>{
    const fr = new FileReader()
    fr.onerror = () => j()
    fr.onload = () => r(fr.result)
    fr.readAsArrayBuffer(f)
  })

  const validate = async file => {
    if (!file) return { ok:false, msg:"Nenhum arquivo selecionado" }

    if (file.size > MAX_BYTES) return { ok:false, msg:"Arquivo maior que o permitido" }

    if (getExt(file.name) !== "xml") return { ok:false, msg:"Somente arquivos .xml" }

    if (!ALLOWED_MIME.has(file.type)) return { ok:false, msg:"MIME type inválido" }

    const buf = await readBuffer(file)
    if (looksBinary(buf)) return { ok:false, msg:"Conteúdo binário detectado" }

    const text = await readText(file)
    const doc = new DOMParser().parseFromString(text, "application/xml")
    if (doc.getElementsByTagName("parsererror").length)
      return { ok:false, msg:"XML malformado" }

    return { ok:true }
  }

  const fail = (i,m) => {
    i.value = ""
    i.setCustomValidity(m)
    i.reportValidity()
  }

  const ok = i => i.setCustomValidity("")

  const inputs = Array.from(document.querySelectorAll('input[type="file"]'))

  inputs.forEach(i => {
    i.addEventListener("change", async () => {
      ok(i)
      try {
        const r = await validate(i.files[0])
        if (!r.ok) fail(i, r.msg)
      } catch {
        fail(i, "Falha na validação do arquivo")
      }
    })

    if (i.form) {
      i.form.addEventListener("submit", async e => {
        ok(i)
        try {
          const r = await validate(i.files[0])
          if (!r.ok) {
            e.preventDefault()
            fail(i, r.msg)
          }
        } catch {
          e.preventDefault()
          fail(i, "Falha na validação do arquivo")
        }
      })
    }
  })
})()
</script>

</body>
</html>

Se mesmo com isso o Fortify continuar marcando, aí você já tem base técnica pra cravar: front está restritivo, inspeção ativa existe, e o upload real é tratado em outro boundary — exatamente o tipo de cenário que SAST costuma marcar por cegueira arquitetural, não por falha real.