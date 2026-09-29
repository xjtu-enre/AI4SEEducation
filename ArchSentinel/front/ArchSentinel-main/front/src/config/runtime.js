// Local-only configuration. Never commit a real key into public/ because every
// file there is downloaded by the browser and becomes part of the repository.
export const CHAT_API_KEY = process.env.VUE_APP_CHAT_API_KEY || ''

