<template>
  <el-card>
    <h2>📊 工作台</h2>
    <el-row :gutter="16" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <h3>在职员工</h3>
          <p style="font-size: 28px; color: #409eff;">1,234</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <h3>本月入职</h3>
          <p style="font-size: 28px; color: #67c23a;">56</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <h3>本月离职</h3>
          <p style="font-size: 28px; color: #f56c6c;">12</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <h3>待审批</h3>
          <p style="font-size: 28px; color: #e6a23c;">8</p>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>
