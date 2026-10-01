import { createRouter, createWebHistory } from 'vue-router'
import HomeView from "@/views/HomeView.vue";
import TestView from "@/views/TestView.vue";
import MyProfile from '@/views/MyProfile.vue'
import AiSearchView from '@/views/AiSearchView.vue'
import ToolsView from '@/views/ToolsView.vue'
import AddToolView from '@/views/AddToolView.vue'
import MyToolsView from '@/views/MyToolsView.vue'
import BookingFormView from '@/views/BookingFormView.vue'
import AdminView from '@/views/AdminView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'homeRoute',
      component: HomeView,
    },
    {
      path: '/test',
      name: 'testRoute',
      component: TestView,
    },
    {
      path: '/profile',
      name: 'profileRoute',
      component: MyProfile,
    },
    {
      path: '/ai-search',
      name: 'aiSearchRoute',
      component: AiSearchView,
    },
    {
      path: '/tools/:toolId/booking',
      name: 'bookingFormRoute',
      component: BookingFormView,
    },
    {
      path: '/tools/new',
      name: 'addToolRoute',
      component: AddToolView,
    },
    {
      path: '/tools',
      name: 'toolsRoute',
      component: ToolsView,
    },
    {
      path: '/my-tools',
      name: 'myToolsRoute',
      component: MyToolsView,
    },
    {
      path: '/admin',
      name: 'adminRoute',
      component: AdminView,
    },
  ],
})

export default router
