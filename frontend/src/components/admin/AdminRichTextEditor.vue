<template>
  <div class="admin-rich-text-editor">
    <QuillEditor
      ref="editorRef"
      v-model:content="innerContent"
      content-type="html"
      theme="snow"
      :options="editorOptions"
      @ready="handleReady"
      @update:content="handleContentUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { QuillEditor } from '@vueup/vue-quill';
import type Quill from 'quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    placeholder?: string;
  }>(),
  {
    modelValue: '',
    placeholder: '请输入正文内容'
  }
);

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void;
}>();

const editorRef = ref<InstanceType<typeof QuillEditor> | null>(null);
const quillInstance = ref<Quill | null>(null);
const innerContent = ref(props.modelValue || '');

watch(
  () => props.modelValue,
  (value) => {
    const nextValue = value || '';
    if (nextValue !== innerContent.value) {
      innerContent.value = nextValue;
    }
  }
);

const toolbarOptions = computed(() => [
  [{ header: [1, 2, 3, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ list: 'ordered' }, { list: 'bullet' }],
  ['blockquote', 'code-block'],
  ['link', 'image'],
  [{ align: [] }],
  ['clean']
]);

const editorOptions = computed(() => ({
  placeholder: props.placeholder,
  modules: {
    toolbar: toolbarOptions.value
  }
}));

function normalizeHtmlContent(value?: string): string {
  return value === '<p><br></p>' ? '' : value || '';
}

function syncEditorContent(): void {
  const html = quillInstance.value?.root.innerHTML || innerContent.value;
  const normalizedValue = normalizeHtmlContent(html);
  innerContent.value = normalizedValue;
  emit('update:modelValue', normalizedValue);
}

function handleReady(quill: Quill): void {
  quillInstance.value = quill;
  syncEditorContent();
}

function handleContentUpdate(): void {
  syncEditorContent();
}
</script>

<style scoped>
:deep(.ql-toolbar.ql-snow) {
  border: 1px solid rgb(203 213 225);
  border-bottom: none;
  border-radius: 16px 16px 0 0;
  background: rgb(248 250 252);
}

:deep(.ql-container.ql-snow) {
  border: 1px solid rgb(203 213 225);
  border-radius: 0 0 16px 16px;
  min-height: 320px;
  font-size: 14px;
  line-height: 1.8;
}

:deep(.ql-editor) {
  min-height: 320px;
  color: rgb(15 23 42);
}

:deep(.ql-editor.ql-blank::before) {
  color: rgb(148 163 184);
  font-style: normal;
}

:deep(.ql-editor img) {
  max-width: 100%;
  height: auto;
  border-radius: 12px;
}

:deep(.ql-editor blockquote) {
  border-left: 4px solid rgb(148 163 184);
  margin: 1em 0;
  padding-left: 1em;
  color: rgb(71 85 105);
}

:deep(.ql-editor pre.ql-syntax) {
  border-radius: 12px;
  background: rgb(15 23 42);
  padding: 16px;
}
</style>
