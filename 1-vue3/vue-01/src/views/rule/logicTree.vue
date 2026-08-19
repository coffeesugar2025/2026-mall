<template>
  <div class="ald-logic-tree-wrap" v-if="treeData">
    <div class="ald-logic-tree-logic-node" draggable="true">
      <template v-for="(node, ind) in treeData?.children">
        <div class="ald-logic-tree-node-wrap">
          <div class="ald-logic-tree-horizontal-line"></div>
          <template v-if="node.logic == 'compare'">
            <div class="ald-logic-tree-record-node" draggable="true">
              <div class="ald-logic-tree-node-drag-icon-preview"></div>
              <div class="ald-logic-tree-render-condition-wrap">
                <div>
                  <div class="wrap___PTX4x">
                    <div class="flex_yc">
                      <!-- 普通节点选项 -->
                      <a-select class="word_select_box" placeholder="请选择字段" v-model:value="node.field" :options="props.tableFieldOptList" />
                      <a-select class="method_select" placeholder="请选择连接方式" v-model:value="node.connectWay" :options="getMethodOptList(node)" />
                      <a-select class="word_select_box" placeholder="请选择字段" v-model:value="node.relevanceField" :options="props.tableFieldOptList" />
 
<!--                      <div class="value_type_select_box" v-if="node.word">
                        <a-select class="method_select" v-model="node.compare">
                          <a-select-option v-for="opt in getMethodOptList(node)" :label="opt.label" :value="opt.value">{{ opt.label }}</a-select-option>
                        </a-select>
                         <template v-if="getNodeWordTypeById(node) == 'date'">
                          <div v-if="isLimitNumber(node.compare)" class="limit_value_box">
                            <a-date-picker v-model="node.equal" type="daterange" range-separator="-" />
                          </div>
                          <div v-else class="input_item">
                            <a-date-picker v-model="node.equal" type="date" />
                          </div>
                        </template>
                        <template v-else-if="getNodeWordTypeById(node) == 'number'">
                          <div v-if="isLimitNumber(node.compare)" class="limit_value_box">
                            <a-input v-if="node.compare" v-model="node.equal[0]" />
                            <a-input v-if="node.compare" v-model="node.equal[1]" />
                          </div>
                          <div v-else class="input_item">
                            <a-input v-model="node.equal" />
                          </div>
                        </template>
                        <template v-else>
                        <div class="input_item">
                          <a-input v-if="node.compare" v-model="node.equal" />
                        </div>
                         </template>
                      </div>-->
                    </div>
                  </div>
                </div>
              </div>
              <div class="ald-icon-button ald-icon-button-small " v-if="!(parentNode == null && treeData?.children?.length == 1)">
                <div class="close_btn">
                  <!-- 删除icon -->
                  <DeleteOutlined name="小弹窗-关闭" size="16px" @click="deleteNodeHandle(node, ind)" />
                </div>
              </div>
            </div>
          </template>
          <template v-else-if="node.type !== 'compare'">
            <LogicTree
              :tree-data="node"
              :parentNode="treeData"
              :tableFieldOptList="props.tableFieldOptList"
              @delete-tree="
                () => {
                  deleteTreeHandle(node, ind)
                }
              "
            />
          </template>
        </div>
      </template>
 
      <div class="ald-logic-tree-operation">
        <div class="ald-logic-tree-left-bottom-dash-line"></div>
        <div class="new_node_box">
          <div class="color_blue add_btn" @click="addNodeHandle(treeData)">
            <PlusOutlined name="add" size="12px" style="color: #126fdd" />
            <span class="text">添加条件</span>
          </div>
          <div class="color_blue add_btn add_relation" @click="addRelationNodeHandle(treeData)">
            <PlusOutlined name="add" size="12px" style="color: #126fdd" />
            <span class="text">添加关系</span>
          </div>
        </div>
      </div>
      <template v-if="treeData.children && treeData.children?.length">
        <div
          class="ald-logic-tree-logic-vertical-line"
          :style="{ height: treeData.children.length > 1 ? 'calc(100% - 82px)' : '30px', top: getVerticalLineTop(), fontWeight: 'normal' }"
        ></div>
      </template>
      <div class="ald-logic-tree-select-relation" spellcheck="false" style="--ald-select-prefix-width: 0px">
        <!-- 关系选项 -->
        <a-select class="relation_select_wrap" v-model:value="treeData.logic"
                  :options="relationOptList" :field-names="{ label: 'label', value: 'value' }">
        </a-select>
      </div>
    </div>
  </div>
<!--  <a-button type="primary" @click="handleSave()">保存</a-button>-->
 
</template>
 
<script lang="ts" setup>
import { ref, onMounted, defineOptions } from 'vue'
import {getTextOptionList, getNumberOptionList, getDateOptionList} from '../../ts/reference'
import type {MapData} from '../../ts/reference'
import type { SQLNodeTree } from '../../ts/reference'
import { DeleteOutlined,PlusOutlined } from '@ant-design/icons-vue';
 
defineOptions({
  name: 'LogicTree',
})
 
interface Props {
  treeData: SQLNodeTree
  tableFieldOptList: MapData[]
  parentNode: SQLNodeTree | null
}
const props = defineProps<Props>()
const emit = defineEmits(['delete-tree'])
const textOptList = ref([])
const numberOptList = ref([])
const dateOptList = ref([])
const relationOptList = ref([
  { label: '且', value: 'and' },
  { label: '或', value: 'or' },
])
 
const treeData = defineModel<SQLNodeTree>('treeData')
const addNodeHandle = (node: SQLNodeTree) => {
  const newNode: SQLNodeTree = {
    logic: 'compare',
    field: '',
    connectWay: '',
    relevanceField: '',
  }
  if (node.children && Array.isArray(node.children)) {
    node.children.push(newNode)
  } else {
    node.children = [newNode]
  }
}
const isLimitNumber = val => {
  return val == 'limit'
}
const isLimitDate = val => {
  return val == 'limit'
}
const getFieldIcon = (opt: MapData) => {
  if (opt.type == 'STRING') {
    return 'abc'
  } else if (opt.type == 'TIMESTAMP') {
    return '日期'
  } else if (opt.type == 'NUMBER') {
    return '123'
  }
  return 'abc'
}
const getMethodOptList = node => {
  const word = node.word
  const opt = props.tableFieldOptList.find(item => item.colName == word)
  if (opt) {
    if (opt.type == 'TIMESTAMP') {
      return getDateOptionList()
    } else if (opt.type == 'NUMBER') {
      return getNumberOptionList()
    }
  }
  return getTextOptionList()
}
 
const addRelationNodeHandle = (node: SQLNodeTree) => {
  const newNode: SQLNodeTree = {
    logic: 'and',
    field: '',
    connectWay: '',
    relevanceField: '',
    children: [
      {
        logic: 'compare',
        field: '',
        connectWay: '',
        relevanceField: '',
      },
    ],
  }
  if (node.children && Array.isArray(node.children)) {
    node.children.push(newNode)
  } else {
    node.children = [newNode]
  }
}
const deleteNodeHandle = (node, ind) => {
  if (treeData.value?.children?.length == 1) {
    if (props.parentNode) {
      emit('delete-tree')
    }
    return
  }
  treeData.value?.children?.splice(ind, 1)
}
const deleteTreeHandle = (node, ind) => {
  if (treeData.value?.children?.length == 1) {
    if (props.parentNode) {
      emit('delete-tree')
    } else {
      treeData.value?.children?.splice(ind, 1)
      addNodeHandle(treeData.value)
    }
    return
  }
  treeData.value?.children?.splice(ind, 1)
}
const getNodeWordTypeById = (node: SQLNodeTree) => {
  const word = node.word
  const opt = props.tableFieldOptList.find(item => item.colName == word)
  if (opt) {
    if (opt.type == 'TIMESTAMP') {
      return 'date'
    } else if (opt.type == 'NUMBER') {
      return 'number'
    }
  }
 
  return 'text'
}
const getVerticalLineTop = () => {
  const topNode = treeData.value
  if (topNode && topNode?.children && topNode?.children?.length) {
    if (topNode?.children[0].logic != 'compare') {
      return '52px'
    }
  }
  return '24px'
}
 
const handleSave = () => {
  console.log("treedata===",treeData.value)
}
 
onMounted(() => {
  textOptList.value = getTextOptionList()
  numberOptList.value = getNumberOptionList()
  dateOptList.value = getDateOptionList()
})
defineExpose({})
</script>
 
<style lang="less" scoped>
.word_select_box {
  min-width: 180px;
}
.method_select {
  margin: 0 5px;
  width: 110px;
}
.close_btn {
  cursor: pointer;
  color: #409eff;
}
.relation_select_wrap {
  width: 60px;
}
.new_node_box {
  padding-left: 8px;
}
.add_btn {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  .text {
    color: #126fdd;
    margin-left: 4px;
    white-space: nowrap;
  }
}
.add_relation {
  margin-left: 20px;
}
.value_type_select_box {
  margin-left: 4px;
  display: flex;
  align-items: center;
  .node_select_box {
    margin-left: 4px;
    width: 120px;
  }
  .limit_value_box {
    margin-left: 4px;
  }
  .input_item {
    margin-left: 4px;
    width: 100px;
  }
}
</style>
<style lang="less" scoped>
.ald-logic-tree-display-logic-node {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 30px;
  position: relative;
}
.ald-logic-tree-display-logic-node .ald-logic-tree-node-wrap {
  position: relative;
}
.ald-logic-tree-display-logic-node .ald-logic-tree-node-wrap .ald-logic-tree-horizontal-line {
  border-top: 1px solid #d1d5db;
  left: -18px;
  position: absolute;
  top: 50%;
  width: 18px;
}
.ald-logic-tree-display-logic-node .ald-logic-tree-logic-type {
  align-items: center;
  background-color: #fff;
  display: flex;
  height: 20px;
  justify-content: center;
  left: 0;
  position: absolute;
  top: calc(50% - 10px);
  width: 24px;
  z-index: 1;
}
.ald-logic-tree-display-logic-node .ald-logic-tree-logic-vertical-line {
  border-left: 1px solid #d1d5db;
  left: 12px;
  position: absolute;
}
.ald-logic-tree-display-record-node {
  align-items: center;
  display: flex;
}
.ald-logic-tree-display-record-node .ald-logic-tree-render-condition-wrap {
  align-items: center;
  display: flex;
  height: 100%;
  width: 100%;
}
 
.ald-logic-tree-logic-node {
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  margin-left: -16px;
  padding-left: 88px;
  position: relative;
  z-index: 10;
}
.ald-logic-tree-logic-node:hover .ald-logic-tree-node-drag-icon,
.ald-logic-tree-logic-node:hover .ald-logic-tree-record-node-drag-icon {
  background: #fff !important;
}
.ald-logic-tree-logic-node.is-hover-drag-box {
  background: #f4f4f5;
}
.ald-logic-tree-logic-node.is-hover-drag-box .ald-logic-tree-node-drag-icon,
.ald-logic-tree-logic-node.is-hover-drag-box .ald-logic-tree-record-node-drag-icon {
  background: #f4f4f5 !important;
}
.ald-logic-tree-logic-node.is-dragging {
  background: var(--alias-colors-bg-selected-default, #e8f2fe);
}
.ald-logic-tree-logic-node.is-dragging .ald-logic-tree-node-drag-icon,
.ald-logic-tree-logic-node.is-dragging .ald-logic-tree-record-node-drag-icon {
  background: var(--alias-colors-bg-selected-default, #e8f2fe) !important;
}
.ald-logic-tree-logic-node .ald-logic-tree-node-drag-icon-wrapper {
  align-items: center;
  cursor: pointer;
  display: flex;
  height: 100%;
  justify-content: center;
  left: 0;
  position: absolute;
  top: 0;
  z-index: 10px;
}
.ald-logic-tree-logic-node .ald-logic-tree-node-wrap {
  position: relative;
  margin-bottom: 4px;
}
.ald-logic-tree-logic-node .ald-logic-tree-node-wrap .ald-logic-tree-horizontal-line {
  border-top: 1px solid #d1d5db;
  left: -48px;
  position: absolute;
  top: 50%;
  width: 48px;
}
.ald-logic-tree-logic-node .ald-logic-tree-operation {
  align-items: center;
  display: flex;
  gap: 10px;
  height: 32px;
  padding: 4px 0;
  margin-top: 12px;
}
.ald-logic-tree-logic-node .ald-logic-tree-operation .ald-logic-tree-left-bottom-dash-line {
  border-bottom: 1px dashed #d1d5db;
  border-left: 1px dashed #d1d5db;
  height: calc(50% - 16px);
  left: 40px;
  position: absolute;
  top: calc(50% - 4px);
  width: 48px;
}
.ald-logic-tree-logic-node .ald-logic-tree-logic-vertical-line {
  border-left: 1px solid #d1d5db;
  left: 40px;
  position: absolute;
}
.ald-logic-tree-logic-node .ald-logic-tree-select-relation {
  background: #fff !important;
  left: 16px;
  position: absolute !important;
  top: calc(50% - 12px);
  width: 48px !important;
  z-index: 1;
}
.ald-logic-tree-logic-node .ald-logic-tree-select-relation .ant-select-selector {
  padding-left: 8px !important;
  padding-right: 0 !important;
}
.ald-logic-tree-logic-node .ald-logic-tree-select-relation .ant-select-selector .ant-select-selection-search {
  right: 0 !important;
}
.ald-logic-tree-logic-node .ald-logic-tree-select-relation .ant-select-arrow {
  right: 6px !important;
}
.ald-logic-tree-record-node {
  align-items: center;
  border-radius: 6px;
  display: flex;
  gap: 6px;
  margin-left: -16px;
  padding-left: 16px;
  position: relative;
  z-index: 10;
}
.ald-logic-tree-record-node.is-hover-drag-box {
  align-items: center;
  background: #f4f4f5;
  display: flex;
}
.ald-logic-tree-record-node.is-hover-drag-box .ald-logic-tree-record-node-drag-icon {
  background: #f4f4f5 !important;
}
.ald-logic-tree-record-node.is-dragging {
  align-items: center;
  background: var(--alias-colors-bg-selected-default, #e8f2fe);
  border-radius: 6px;
  display: flex;
}
.ald-logic-tree-record-node.is-dragging .ald-logic-tree-record-node-drag-icon {
  background: var(--alias-colors-bg-selected-default, #e8f2fe) !important;
}
.ald-logic-tree-record-node .ald-logic-tree-record-node-drag-icon-wrapper {
  align-items: center;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  height: 100%;
  justify-content: center;
  left: 0;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  z-index: 10;
}
.ald-logic-tree-record-node .ald-logic-tree-record-node-drag-icon-wrapper .ald-logic-tree-record-node-drag-icon {
  background-color: #fff;
}
.ald-logic-tree-record-node .ald-logic-tree-render-condition-wrap {
  align-items: center;
  display: flex;
  flex: 0 0 fit-content;
}
.ald-logic-tree-logic-node,
.ald-logic-tree-record-node {
  padding-bottom: 4px;
  padding-top: 4px;
  position: relative;
}
.ald-logic-tree-logic-node.is-hover-bottom:last-child:after,
.ald-logic-tree-record-node.is-hover-bottom:last-child:after {
  background-color: #126fdd;
  bottom: -1px;
  content: '';
  height: 2px;
  left: 16px;
  position: absolute;
  width: calc(100% - 16px);
}
.ald-logic-tree-logic-node.is-hover-top:last-child:after,
.ald-logic-tree-record-node.is-hover-top:last-child:after {
  background-color: #126fdd;
  content: '';
  height: 2px;
  left: 16px;
  position: absolute;
  top: -1px;
  width: calc(100% - 16px);
}
.ald-logic-tree-node-drag-icon-preview {
  display: none;
}
</style>
 
 