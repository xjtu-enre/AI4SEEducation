// router/index.js
import Vue from 'vue';
import Router from 'vue-router';

import HomePage from '@/components/architecturePages/HomePage.vue';
import ViolationInfoPage from '@/components/architecturePages/ViolationInfoPage.vue';
import EntityRelationPage from '@/components/architecturePages/EntityRelationPage.vue';
import DrawUmlPage from '@/components/architecturePages/DrawUmlPage.vue';
import TestPage from '@/components/architecturePages/TestPage.vue';

Vue.use(Router)

const routes = [
    {
        path: '/',
        name: 'HomePage',
        component: () => import('../views/HomePage.vue')
    },
    { path: '/home-page', component: HomePage },
    { path: '/check-result', component: ViolationInfoPage },
    { path: '/entity-relation', component: EntityRelationPage },
    { path: '/draw-uml', component: DrawUmlPage },
    { path: '/test', component: TestPage }
]

const router = new Router({
    routes
})

export default router
