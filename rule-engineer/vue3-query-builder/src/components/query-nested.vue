<template>
    <draggable
        class="qb-draggable qb-rules-container"
        :list="items"
        :group="{ name: 'g1' }"
        item-key="uuid"
        v-bind="dragOptions"
        :component-data="{name:'fade'}"
        handle=".qb-rule-handle"
        :move="onMove"
    >
        <template #item="{ element, index}">
            <div class="qb-item" :class="_isGroup(element)" :data-id="element?.type?.identifier">
                <queryItem  :rule="element" :index="index" 
                            :calculatedLevel="calculatedLevel"
                            :config="config" 
                            :parentuuid="parentuuid"
                            :levelOperators="levelOperators"
                            @delete-rule="$emit('delete-rule', $event)"
                            @update-rule="$emit('update-rule', $event)"
                            @level-operator-value="$emit('level-operator-value', $event)">
                </queryItem>
                
                <nestedQuery    v-if="element.isGroup" 
                                :class="'nested-group level-' + calculatedLevel + ' ' + element.identifier" 
                                :config="config"
                                :parentuuid="element.uuid"
                                :calculatedLevel="newCalculatedLevel" 
                                :levelOperators="levelOperators"
                                @delete-rule="$emit('delete-rule', $event)"
                                @update-rule="$emit('update-rule', $event)"
                                @add-rule="$emit('add-rule', $event)"
                                @level-operator-value="$emit('level-operator-value', $event)"
                                :items="element.children">
                                <template #addRule>
                                    <slot name="addRule">
                                    </slot>
                                </template>
                                <template #addGroup>
                                    <slot name="addGroup">
                                    </slot>
                                </template>
                </nestedQuery>

                <queryAdd   v-if="element.children" 
                            :index="index"
                            :calculatedLevel="newCalculatedLevel"
                            :config="config" 
                            :parentuuid="element.uuid"
                            @add-rule="$emit('add-rule', $event)">
                        <template #addRule>
                            <slot name="addRule">
                            </slot>
                        </template>
                        <template #addGroup>
                            <slot name="addGroup">
                            </slot>
                        </template>
                </queryAdd>
            </div>
        </template>
    </draggable>
</template>
<script setup>
import draggable from "vuedraggable";
import { computed, reactive} from 'vue';

defineOptions({
    name: 'nested-query'
})

defineEmits(['add-rule', 'delete-rule', 'level-operator-value', 'update-rule'])

const props = defineProps({
    items: {
        type: Array
    },
    calculatedLevel: {
        type: Number
    },
    config:{
        type: Object,
    },
    parentuuid: {
        type: String
    },
    levelOperators: {
        type: Array
    }
})

const _isGroup = (element) => { if(element.isGroup){ return 'qb-item__group ' + element.identifier }}

const newCalculatedLevel = computed(() => props.calculatedLevel+1)

const dragOptions = reactive({
    animation: 200,
    group: "query",
    disabled: false,
    ghostClass: "ghost",
    dataIdAttr: 'data-id', 
})
function onMove(event){

    var _classes = event.to.className.split(' ').map(str => /qb-rules-container/.test(str)).findIndex(i => i === true)
    let _isGroup = event.to.className.split(' ').map(str => /group/.test(str)).findIndex(i => i === true) 
    
    if(_isGroup > 0 || _classes > 0){
        return true
    } else {
        return false
    }
}


</script>