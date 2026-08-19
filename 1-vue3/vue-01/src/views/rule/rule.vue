<template>
  <div>
    <LogicTree
      :tree-data="initialTreeData"
      :table-field-opt-list="tableFieldOptList"
    />
  </div>
</template>
 
<script setup lang="ts">
import LogicTree from "@/views/rule/logicTree.vue";
import { ref } from 'vue';
import type {MapData, SQLNodeTree} from '@/ts/reference'
 
 
// 定义初始数据
const initialTreeData = ref<SQLNodeTree>({
  logic: 'and',
  children: [
    {
      logic: 'compare',
      field: '',
      connectWay: '',
      relevanceField: '',
    }
  ]
});
 
const tableFieldOptList = ref<MapData[]>([
  { label: '字段1', colName: 'field1', type: 'STRING', value: 'field1' },
  { label: '字段2', colName: 'field2', type: 'NUMBER', value: 'field2' },
  { label: '字段3', colName: 'field3', type: 'TIMESTAMP', value: 'field3' }
]);
 
// 其他逻辑...
 
</script>