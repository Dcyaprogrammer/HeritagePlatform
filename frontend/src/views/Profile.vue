<template>
  <div class="profile-page">
    <section class="profile-hero public-panel">
      <div class="profile-hero__toolbar">
        <button type="button" class="public-btn public-btn--ghost" @click="goBack">
          <span aria-hidden="true">←</span>
          Back
        </button>
        <button type="button" class="public-btn public-btn--ghost profile-home-btn" @click="router.push('/')">
          <el-icon><House /></el-icon>
          Archive Home
        </button>
      </div>

      <div class="profile-hero__content" v-loading="loading">
        <div class="profile-hero__identity">
          <div class="profile-stamp" aria-hidden="true">{{ profileInitial }}</div>
          <div class="profile-hero__copy">
            <p class="public-eyebrow">Personal Archive</p>
            <h1 class="profile-title">{{ profileDisplayName }}</h1>
            <p class="profile-subtitle">@{{ form.username || 'user' }}</p>
            <p class="profile-lead">
              {{ profileBio }}
            </p>
          </div>
        </div>

        <div class="profile-hero__facts">
          <div class="fact-card">
            <span class="fact-label">Primary role</span>
            <strong class="fact-value">{{ primaryRoleLabel }}</strong>
          </div>
          <div class="fact-card">
            <span class="fact-label">Contributor access</span>
            <strong class="fact-value">{{ contributorStateLabel }}</strong>
          </div>
          <div class="fact-card fact-card--wide">
            <span class="fact-label">Contact</span>
            <strong class="fact-value fact-value--small">{{ form.email || 'No email recorded' }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="profile-meta-strip">
      <div class="meta-block public-panel">
        <span class="meta-block__label">Roles</span>
        <div class="meta-block__content">
          <span v-for="role in userRoles" :key="role" class="profile-role-chip" :class="`profile-role-chip--${role.toLowerCase()}`">
            {{ formatRole(role) }}
          </span>
          <span v-if="!userRoles.length" class="profile-role-chip">Viewer</span>
        </div>
      </div>

      <div class="meta-block public-panel" :class="`meta-block--${contributorTone}`">
        <span class="meta-block__label">Contributor status</span>
        <div class="meta-block__content meta-block__content--status">
          <strong class="status-title">{{ contributorTitle }}</strong>
          <p class="status-copy">{{ contributorDescription }}</p>
        </div>
      </div>
    </section>

    <section class="profile-grid">
      <section class="profile-panel profile-panel--editor public-panel" v-loading="loading">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Profile</p>
            <h2 class="panel-title">Personal details</h2>
          </div>
          <p class="panel-note">Adjust how your account is presented across the archive.</p>
        </div>

        <el-form :model="form" label-position="top" class="profile-form">
          <div class="profile-form__grid">
            <el-form-item label="Username">
              <el-input v-model="form.username" disabled />
            </el-form-item>

            <el-form-item label="Email">
              <el-input v-model="form.email" />
            </el-form-item>
          </div>

          <div class="profile-form__grid">
            <el-form-item label="Display Name">
              <el-input v-model="form.displayName" />
            </el-form-item>
          </div>

          <el-form-item label="Bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="5"
              resize="none"
              placeholder="Tell visitors a little about your perspective, practice, or research focus."
            />
          </el-form-item>

          <div class="panel-actions">
            <button type="button" class="public-btn public-btn--primary" @click="handleSave" :disabled="saving">
              {{ saving ? 'Saving…' : 'Save changes' }}
            </button>
          </div>
        </el-form>
      </section>

      <section class="profile-panel profile-panel--status public-panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Access</p>
            <h2 class="panel-title">Contributor pathway</h2>
          </div>
          <p class="panel-note">Manage publishing privileges and contribution access.</p>
        </div>

        <div class="contributor-state" :class="`contributor-state--${contributorTone}`">
          <div class="contributor-state__icon">
            <el-icon v-if="contributorTone === 'warning'"><Timer /></el-icon>
            <el-icon v-else-if="contributorTone === 'danger'"><CircleClose /></el-icon>
            <el-icon v-else><CircleCheck /></el-icon>
          </div>
          <div class="contributor-state__body">
            <strong class="contributor-state__title">{{ contributorTitle }}</strong>
            <p class="contributor-state__copy">{{ contributorDescription }}</p>
          </div>
        </div>

        <div class="role-overview">
          <span class="role-overview__label">Current access scope</span>
          <div class="role-overview__chips">
            <span v-for="role in userRoles" :key="role" class="profile-role-chip" :class="`profile-role-chip--${role.toLowerCase()}`">
              {{ formatRole(role) }}
            </span>
          </div>
        </div>

        <div class="contributor-cta">
          <p class="panel-note panel-note--compact">
            Contributors can draft, submit, and manage heritage resources directly from the archive workspace.
          </p>
          <button
            v-if="!isContributor"
            type="button"
            class="public-btn public-btn--primary"
            @click="showApplyDialog = true"
          >
            {{ contributorStatus === 'REJECTED' ? 'Reapply for contributor access' : 'Apply for contributor access' }}
          </button>
        </div>
      </section>

      <section class="profile-panel profile-panel--security public-panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Security</p>
            <h2 class="panel-title">Change password</h2>
          </div>
          <p class="panel-note">Keep your account secure by refreshing credentials when needed.</p>
        </div>

        <el-form :model="pwdForm" label-position="top" :rules="pwdRules" ref="pwdFormRef" class="profile-form">
          <div class="profile-form__grid">
            <el-form-item label="Current Password" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>

            <el-form-item label="New Password" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
          </div>

          <div class="panel-actions">
            <button type="button" class="public-btn" @click="handleChangePassword" :disabled="changingPwd">
              {{ changingPwd ? 'Updating…' : 'Update password' }}
            </button>
          </div>
        </el-form>
      </section>

      <section class="profile-panel profile-panel--sessions public-panel">
        <div class="panel-head">
          <div>
            <p class="panel-kicker">Sessions</p>
            <h2 class="panel-title">Device control</h2>
          </div>
          <p class="panel-note">Review active sign-ins and remove sessions you no longer trust.</p>
        </div>

        <div class="sessions-callout">
          <div class="sessions-callout__copy">
            <strong>Multi-session control</strong>
            <p>
              Open the session manager to inspect recent logins, revoke forgotten devices, and keep your account perimeter clean.
            </p>
          </div>
          <button type="button" class="public-btn public-btn--ghost" @click="router.push({ name: 'Sessions' })">
            Manage active sessions
          </button>
        </div>
      </section>
    </section>

    <el-dialog v-model="showApplyDialog" title="Apply to be a Contributor" width="560px" class="profile-dialog">
      <div class="dialog-copy">
        <p>
          Tell the archive team what kinds of materials you plan to contribute and why contributor access is appropriate for your work.
        </p>
      </div>
      <el-form :model="applyForm" label-position="top">
        <el-form-item label="Application reason">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="5"
            resize="none"
            placeholder="Describe your contribution intent, domain knowledge, or collection focus."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <button type="button" class="public-btn public-btn--ghost" @click="showApplyDialog = false">Cancel</button>
        <button type="button" class="public-btn public-btn--primary" @click="handleApply" :disabled="applying">
          {{ applying ? 'Submitting…' : 'Submit application' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Timer, CircleClose, CircleCheck, House } from '@element-plus/icons-vue'
import { getCurrentUser } from '../api/auth.js'
import { getUser, updateUser, updatePassword, applyContributor } from '../api/user.js'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const changingPwd = ref(false)
const applying = ref(false)
const pwdFormRef = ref(null)
const showApplyDialog = ref(false)

const form = reactive({
  username: '',
  displayName: '',
  email: '',
  bio: ''
})

const userRoles = ref([])
const contributorStatus = ref('NONE')

const pwdForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const applyForm = reactive({
  reason: ''
})

const isContributor = computed(() => userRoles.value.includes('CONTRIBUTOR'))
const profileDisplayName = computed(() => form.displayName?.trim() || form.username || 'Archive Member')
const profileBio = computed(() => form.bio?.trim() || 'A concise profile helps collaborators and reviewers understand your perspective within the archive.')
const profileInitial = computed(() => (profileDisplayName.value || 'A').trim().charAt(0).toUpperCase())
const primaryRoleLabel = computed(() => formatRole(userRoles.value[0] || 'VIEWER'))
const contributorStateLabel = computed(() => {
  if (isContributor.value) return 'Enabled'
  if (contributorStatus.value === 'PENDING') return 'Pending review'
  if (contributorStatus.value === 'REJECTED') return 'Reapply available'
  return 'Not enabled'
})
const contributorTone = computed(() => {
  if (isContributor.value) return 'success'
  if (contributorStatus.value === 'PENDING') return 'warning'
  if (contributorStatus.value === 'REJECTED') return 'danger'
  return 'neutral'
})
const contributorTitle = computed(() => {
  if (isContributor.value) return 'Contributor access is active'
  if (contributorStatus.value === 'PENDING') return 'Application under review'
  if (contributorStatus.value === 'REJECTED') return 'Application needs revision'
  return 'Contributor access not requested'
})
const contributorDescription = computed(() => {
  if (isContributor.value) return 'You can create drafts, submit records for review, and manage your published contributions.'
  if (contributorStatus.value === 'PENDING') return 'An administrator is reviewing your request. You will be able to contribute once the application is approved.'
  if (contributorStatus.value === 'REJECTED') return 'Your previous request was not approved. You can submit a stronger application with more context.'
  return 'Apply for contributor access if you plan to submit and maintain heritage resources in the public archive.'
})

const pwdRules = {
  oldPassword: [{ required: true, message: 'Please enter current password', trigger: 'blur' }],
  newPassword: [{ required: true, message: 'Please enter new password', trigger: 'blur' }]
}

function formatRole(role) {
  const map = {
    ADMIN: 'Administrator',
    CONTRIBUTOR: 'Contributor',
    VIEWER: 'Viewer',
  }
  return map[role] || role
}

const goBack = () => {
  router.go(-1)
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const username = localStorage.getItem('username')
    let res
    if (username) {
      res = await getUser(username)
    } else {
      res = await getCurrentUser()
    }
    const data = res.data.data
    form.username = data.username
    form.displayName = data.displayName || ''
    form.email = data.email || ''
    form.bio = data.bio || ''
    userRoles.value = data.roles || []
    contributorStatus.value = data.contributorStatus || 'NONE'
  } catch {
    ElMessage.error('Failed to load profile')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateUser(form.username, {
      displayName: form.displayName,
      email: form.email,
      bio: form.bio
    })
    ElMessage.success('Profile updated')
  } catch {
    ElMessage.error('Failed to update profile')
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    changingPwd.value = true
    try {
      await updatePassword(form.username, {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('Password changed successfully')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
    } catch (error) {
      ElMessage.error(error.response?.data?.message || 'Failed to change password')
    } finally {
      changingPwd.value = false
    }
  })
}

const handleApply = async () => {
  applying.value = true
  try {
    await applyContributor(form.username, applyForm.reason)
    contributorStatus.value = 'PENDING'
    showApplyDialog.value = false
    applyForm.reason = ''
    ElMessage.success('Application submitted. Waiting for admin approval.')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to submit application')
  } finally {
    applying.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-page {
  width: 100%;
  padding: 1.25rem 0 2.5rem;
}

.profile-hero {
  position: relative;
  overflow: hidden;
  padding: 1.2rem 1.2rem 1.35rem;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--accent) 10%, transparent), transparent 28%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-raised) 92%, white 8%), var(--surface));
}

.profile-hero::after {
  content: "";
  position: absolute;
  inset: 1rem;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 45%, white 55%);
  pointer-events: none;
}

.profile-hero__toolbar,
.profile-hero__content,
.profile-meta-strip,
.profile-grid {
  position: relative;
  z-index: 1;
}

.profile-hero__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 0.85rem;
  flex-wrap: wrap;
  margin-bottom: 1.35rem;
}

.profile-home-btn :deep(.el-icon) {
  font-size: 1rem;
}

.profile-hero__content {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(280px, 0.9fr);
  gap: 1.5rem;
  align-items: end;
}

.profile-hero__identity {
  display: flex;
  gap: 1.15rem;
  align-items: flex-start;
}

.profile-stamp {
  width: 5.6rem;
  height: 5.6rem;
  border-radius: 22px;
  display: grid;
  place-items: center;
  background:
    linear-gradient(145deg, color-mix(in srgb, var(--accent) 85%, #95614f 15%), color-mix(in srgb, var(--accent-soft) 82%, #704230 18%));
  color: #fff8f3;
  font-family: var(--font-serif);
  font-size: 2.35rem;
  font-weight: 700;
  box-shadow: 0 18px 28px rgba(90, 34, 24, 0.16);
}

.profile-hero__copy {
  min-width: 0;
}

.profile-title {
  margin: 0;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: clamp(2rem, 3vw, 2.7rem);
  font-weight: 700;
  line-height: 1.02;
}

.profile-subtitle {
  margin: 0.45rem 0 0;
  color: var(--accent-soft);
  font-size: 0.92rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.profile-lead {
  max-width: 44rem;
  margin: 0.9rem 0 0;
  color: var(--ink-soft);
  font-size: 1rem;
  line-height: 1.8;
}

.profile-hero__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.9rem;
}

.fact-card {
  min-height: 7.25rem;
  padding: 1rem 1.05rem;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 60%, white 40%);
  background: color-mix(in srgb, var(--surface-raised) 88%, white 12%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.fact-card--wide {
  grid-column: 1 / -1;
  min-height: 6.1rem;
}

.fact-label {
  color: var(--muted);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.fact-value {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 1.32rem;
  font-weight: 700;
  line-height: 1.25;
}

.fact-value--small {
  font-size: 1.02rem;
  font-family: var(--font-sans);
  font-weight: 600;
}

.profile-meta-strip {
  margin-top: 1rem;
  display: grid;
  grid-template-columns: minmax(0, 0.85fr) minmax(0, 1.15fr);
  gap: 1rem;
}

.meta-block {
  min-height: 9.25rem;
  padding: 1rem 1.1rem;
  border-radius: 22px;
}

.meta-block__label {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  color: var(--muted);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.meta-block__label::before {
  content: "";
  width: 0.48rem;
  height: 0.48rem;
  border-radius: 999px;
  background: color-mix(in srgb, var(--accent) 42%, white 58%);
}

.meta-block__content {
  margin-top: 1rem;
}

.meta-block__content--status {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.meta-block--success {
  background: linear-gradient(180deg, color-mix(in srgb, var(--success-soft) 72%, var(--surface-raised) 28%), var(--surface));
}

.meta-block--warning {
  background: linear-gradient(180deg, color-mix(in srgb, #fff0d5 80%, var(--surface-raised) 20%), var(--surface));
}

.meta-block--danger {
  background: linear-gradient(180deg, color-mix(in srgb, var(--danger-soft) 72%, var(--surface-raised) 28%), var(--surface));
}

.status-title {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 1.22rem;
  font-weight: 700;
}

.status-copy {
  margin: 0;
  color: var(--ink-soft);
  font-size: 0.94rem;
  line-height: 1.7;
}

.profile-role-chip {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  margin: 0 0.45rem 0.45rem 0;
  padding: 0.2rem 0.75rem;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 55%, var(--border));
  background: color-mix(in srgb, var(--surface-raised) 90%, white 10%);
  color: var(--ink-soft);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.profile-role-chip--admin {
  border-color: color-mix(in srgb, var(--danger) 30%, var(--border));
  color: var(--danger);
}

.profile-role-chip--contributor {
  border-color: color-mix(in srgb, var(--success) 30%, var(--border));
  color: var(--success);
}

.profile-grid {
  margin-top: 1rem;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 1rem;
}

.profile-panel {
  min-width: 0;
  padding: 1.2rem 1.25rem;
  border-radius: 24px;
}

.profile-panel--editor {
  grid-row: span 2;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.panel-kicker {
  margin: 0 0 0.35rem;
  color: var(--accent-soft);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.panel-title {
  margin: 0;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 1.55rem;
  font-weight: 700;
  line-height: 1.15;
}

.panel-note {
  max-width: 19rem;
  margin: 0;
  color: var(--muted);
  font-size: 0.88rem;
  line-height: 1.65;
  text-align: right;
}

.panel-note--compact {
  max-width: none;
  text-align: left;
}

.profile-form {
  width: 100%;
}

.profile-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 1rem;
}

.panel-actions {
  margin-top: 0.4rem;
  display: flex;
  justify-content: flex-end;
}

.contributor-state {
  display: grid;
  grid-template-columns: 3rem minmax(0, 1fr);
  gap: 0.9rem;
  padding: 1rem 1.05rem;
  border-radius: 20px;
  border: 1px solid color-mix(in srgb, var(--border) 75%, white 25%);
  background: color-mix(in srgb, var(--surface-raised) 82%, white 18%);
}

.contributor-state--success {
  background: linear-gradient(180deg, color-mix(in srgb, var(--success-soft) 78%, var(--surface-raised) 22%), var(--surface-raised));
}

.contributor-state--warning {
  background: linear-gradient(180deg, color-mix(in srgb, #fff0d5 82%, var(--surface-raised) 18%), var(--surface-raised));
}

.contributor-state--danger {
  background: linear-gradient(180deg, color-mix(in srgb, var(--danger-soft) 78%, var(--surface-raised) 22%), var(--surface-raised));
}

.contributor-state__icon {
  width: 3rem;
  height: 3rem;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: color-mix(in srgb, var(--surface-raised) 72%, white 28%);
  color: var(--accent);
  font-size: 1.2rem;
}

.contributor-state__title {
  color: var(--ink);
  font-size: 1rem;
  font-weight: 700;
}

.contributor-state__copy {
  margin: 0.35rem 0 0;
  color: var(--ink-soft);
  font-size: 0.92rem;
  line-height: 1.65;
}

.role-overview {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid color-mix(in srgb, var(--border) 65%, transparent);
}

.role-overview__label {
  display: block;
  margin-bottom: 0.65rem;
  color: var(--muted);
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.contributor-cta {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.85rem;
}

.sessions-callout {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem 1.05rem;
  border-radius: 20px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 50%, var(--border));
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-raised) 88%, white 12%), var(--surface));
}

.sessions-callout__copy strong {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 1.08rem;
  font-weight: 700;
}

.sessions-callout__copy p,
.dialog-copy p {
  margin: 0.55rem 0 0;
  color: var(--ink-soft);
  font-size: 0.92rem;
  line-height: 1.7;
}

.profile-dialog :deep(.el-dialog__body) {
  padding-top: 0.75rem;
}

@media (max-width: 980px) {
  .profile-hero__content,
  .profile-meta-strip,
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-panel--editor {
    grid-row: auto;
  }
}

@media (max-width: 720px) {
  .profile-page {
    padding-top: 0.75rem;
  }

  .profile-hero {
    padding: 1rem;
    border-radius: 24px;
  }

  .profile-hero__identity {
    flex-direction: column;
  }

  .profile-hero__facts {
    grid-template-columns: 1fr;
  }

  .profile-form__grid,
  .panel-head {
    grid-template-columns: 1fr;
  }

  .panel-head {
    display: grid;
  }

  .panel-note {
    max-width: none;
    text-align: left;
  }
}
</style>
