import { createRouter, createWebHistory } from 'vue-router'
import ChatView from '../views/ChatView.vue'
import KnowledgeView from '../views/KnowledgeView.vue'
import IncidentView from '../views/IncidentView.vue'
import InvestView from '../views/InvestView.vue'
import MonitorView from '../views/MonitorView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/chat' },
    { path: '/chat', component: ChatView },
    { path: '/knowledge', component: KnowledgeView },
    { path: '/incident', component: IncidentView },
    { path: '/invest', component: InvestView },
    { path: '/monitor', component: MonitorView }
  ]
})
