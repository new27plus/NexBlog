import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import SystemArticleListView from '@/views/SystemArticleListView.vue'
import ArticleDetailView from '@/views/ArticleDetailView.vue'
import StudioArticleView from '@/views/StudioArticleView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'portal-home',
      component: HomeView
    },
    {
      path: '/app/articles',
      name: 'system-articles',
      component: SystemArticleListView
    },
    {
      path: '/app/articles/:id',
      name: 'article-detail',
      component: ArticleDetailView
    },
    {
      path: '/app/studio/articles',
      name: 'studio-articles',
      component: StudioArticleView
    },
    {
      path: '/articles/:id',
      redirect: to => `/app/articles/${to.params.id}`
    },
    {
      path: '/admin/articles',
      redirect: '/app/studio/articles'
    },
    {
      path: '/app/admin/articles',
      redirect: '/app/studio/articles'
    },
  ],
})

export default router
