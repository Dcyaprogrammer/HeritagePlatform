const envLoggedIn = import.meta.env.VITE_DEV_LOGGED_IN

function toBoolean(value: string | undefined, fallback: boolean) {
  if (value == null) return fallback
  const normalized = value.trim().toLowerCase()
  if (['1', 'true', 'yes', 'on'].includes(normalized)) return true
  if (['0', 'false', 'no', 'off'].includes(normalized)) return false
  return fallback
}

export const devAuth = {
  isLoggedIn: toBoolean(envLoggedIn, import.meta.env.DEV),
  userId: Number(import.meta.env.VITE_DEV_USER_ID ?? 1),
  userName: import.meta.env.VITE_DEV_USER_NAME ?? 'Demo user',
}
