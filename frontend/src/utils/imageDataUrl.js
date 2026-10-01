// Backend saadab pildi puhta Base64 stringina ilma MIME tüübita, seega tuvastame tüübi baitide algusest.
export function toImageDataUrl(base64) {
  const data = base64?.trim()
  if (!data) return ''
  try {
    const prefix = atob(data.slice(0, 160)).trimStart()
    if (prefix.startsWith('<svg') || prefix.startsWith('<?xml')) return `data:image/svg+xml;base64,${data}`
    if (prefix.startsWith('\x89PNG')) return `data:image/png;base64,${data}`
    if (prefix.startsWith('\xff\xd8')) return `data:image/jpeg;base64,${data}`
    if (prefix.startsWith('GIF8')) return `data:image/gif;base64,${data}`
    if (prefix.startsWith('RIFF') && prefix.slice(8, 12) === 'WEBP') return `data:image/webp;base64,${data}`
  } catch {
    return ''
  }
  return ''
}
