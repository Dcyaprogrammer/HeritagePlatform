export const PASSWORD_POLICY_MESSAGE = 'Password must be at least 6 characters and include both letters and numbers'

export function validatePasswordPolicy(rule, value, callback) {
  const hasLetter = /[A-Za-z]/.test(value || '')
  const hasDigit = /\d/.test(value || '')

  if (!value) {
    callback()
    return
  }

  if (value.length < 6 || !hasLetter || !hasDigit) {
    callback(new Error(PASSWORD_POLICY_MESSAGE))
    return
  }

  callback()
}
