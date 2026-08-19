// vhr/src/router/index.js
// ✅ 这个文件现在不再单独导出 router 实例了
// 路由创建逻辑已移入 main.js 的 render() 函数中
// 保留此文件仅作说明，你可以删除它或留着当路由表参考

export const routes = [
  {
    path: '/',
    redirect: '/employee/list',
  },
  {
    path: '/employee/list',
    name: 'EmployeeList',
    component: () => import('@/views/EmployeeList.vue'),
    meta: { title: '员工列表' },
  },
  {
    path: '/employee/detail/:id',
    name: 'EmployeeDetail',
    component: () => import('@/views/EmployeeDetail.vue'),
    meta: { title: '员工档案' },
  },
];