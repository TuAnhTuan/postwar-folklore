/**
 * Thêm Cloudinary transformation vào URL để resize/crop server-side.
 * @param {string} url - URL gốc từ Cloudinary
 * @param {object} opts
 * @param {number} opts.w - width (px)
 * @param {number} opts.h - height (px)
 * @param {string} opts.crop - chế độ crop: 'fill' | 'fit' | 'scale' (default: 'fill')
 * @param {boolean} opts.gravity - dùng g_auto (smart crop) hay không (default: true)
 */
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
