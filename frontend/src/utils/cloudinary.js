const CLOUD_NAME   = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME
const UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET

export async function uploadToCloudinary(file) {
  const fd = new FormData()
  fd.append('file', file)
  fd.append('upload_preset', UPLOAD_PRESET)
  fd.append('folder', 'truyen-thuyet-hau-chien')

  const res = await fetch(`https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`, {
    method: 'POST',
    body: fd
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.error?.message || 'Cloudinary upload failed')
  }
  const data = await res.json()
  return data.secure_url
}

export function cloudinaryUrl(url, { w, h, crop = 'fill', gravity = true } = {}) {
  if (!url || !url.includes('cloudinary.com')) return url

  const parts = []
  parts.push(`c_${crop}`)
  if (w) parts.push(`w_${w}`)
  if (h) parts.push(`h_${h}`)
  if (gravity && crop === 'fill') parts.push('g_auto')
  parts.push('f_auto')
  parts.push('q_auto')

  return url.replace('/upload/', `/upload/${parts.join(',')}/`)
}
