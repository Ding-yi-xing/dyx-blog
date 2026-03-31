import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import './styles/index.scss';

/**
 * 前端应用入口。
 * <p>
 * 该入口负责创建 Vue 应用实例，按顺序挂载 Pinia、路由系统与 Element Plus，
 * 最终将整站应用挂载到页面根节点。
 * </p>
 *
 * @author Dyx
 */
const dyxApp = createApp(App);
const dyxPinia = createPinia();

dyxApp.use(dyxPinia);
dyxApp.use(router);
dyxApp.use(ElementPlus);
dyxApp.mount('#app');
