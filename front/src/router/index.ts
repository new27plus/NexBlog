import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import ArticleDetailView from '@/views/ArticleDetailView.vue'
import StudioArticleView from '@/views/StudioArticleView.vue'
import StudioCategoryView from '@/views/StudioCategoryView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'blog-home',
      component: HomeView
    },
    {
      path: '/articles/:id',
      name: 'article-detail',
      component: ArticleDetailView
    },
    {
      path: '/studio/articles',
      name: 'studio-articles',
      component: StudioArticleView
    },
    {
      path: '/studio/categories',
      name: 'studio-categories',
      component: StudioCategoryView
    },
    {
      path: '/app/articles',
      redirect: '/'
    },
    {
      path: '/app/articles/:id',
      redirect: to => `/articles/${to.params.id}`
    },
    {
      path: '/admin/articles',
      redirect: '/studio/articles'
    },
    {
      path: '/app/admin/articles',
      redirect: '/studio/articles'
    },
    {
      path: '/admin/categories',
      redirect: '/studio/categories'
    },
    {
      path: '/app/admin/categories',
      redirect: '/studio/categories'
    },
    {
      path: '/app/studio/articles',
      redirect: '/studio/articles'
    },
    {
      path: '/app/studio/categories',
      redirect: '/studio/categories'
    },
  ],
})

export default router
