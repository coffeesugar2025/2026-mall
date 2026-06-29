<template>
    <div class="qb-container">     
        <nestedQuery    :items="currentQuery.rules" 
                        :calculatedLevel="calculatedLevel" 
                        :config="config" 
                        :levelOperators="currentQuery.levelOperators"
                        @delete-rule="(event) => deleteRule(event)"
                        @update-rule="(event) => updateRule(event)"
                        @level-operator-value="(event) => setLevelOperator(event)"
                        @add-rule="(event) => addRule(event)">
                        <template #addRule>
                            <slot name="addRule">
                            </slot>
                        </template>
                        <template #addGroup>
                            <slot name="addGroup">
                            </slot>
                        </template>
        </nestedQuery>

        <div class="qb-actions">
            <queryAdd   :config="config" 
                        :calculatedLevel="calculatedLevel" 
                        @add-rule="(item) => addRule(item)">
                        <template #addRule>
                            <slot name="addRule">
                            </slot>
                        </template>
                        <template #addGroup>
                            <slot name="addGroup">
                            </slot>
                        </template>
            </queryAdd>
            <button v-if="currentQuery?.rules.length" type="button" 
                    class="btn btn-light qb-actions__delete qb-btn" 
                    @click="currentQuery.rules = []">
                    <slot name="removeRules">delete all Rules</slot>
            </button>
        </div>
    </div>
</template>
<script setup>
import { reactive, watch, onMounted } from 'vue';

defineOptions({
    name: 'advanced-query-builder'
})

const props = defineProps({
    config: {
        type: Object,
    },
    cssModifier: {
        type: String,
        default: 'item'
    },
    modelValue: {
        type: Object
    }
})
const currentQuery = reactive ({
    levelOperators: [],
    rules: [],
})
const calculatedLevel = 0
const emit = defineEmits(['update:model-value'])

watch(currentQuery, (oldQuery, newQuery) => {
    emit('update:model-value', currentQuery)
})

watch(() => props.modelValue, (first, second) => {
    setModelValue()
});

onMounted(() => {
    setModelValue()
})

function setModelValue(){
    if(props.modelValue){
        currentQuery.rules = [...props.modelValue?.rules] || []
        currentQuery.levelOperators = [...props.modelValue?.levelOperators] || []
    }
}

function getUUID(){
    // https://stackoverflow.com/questions/105034/how-do-i-create-a-guid-uuid
    return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
}

function findRule(obj, _uuid){
    for(const i in obj){
        if(obj[i].uuid == _uuid) return obj[i]
        if(obj[i].children){
            const nestedObj = findRule(obj[i].children, _uuid)
            if (nestedObj) return nestedObj;
        }
    }
}

function selectRule(_id){
    if(_id === 'group'){
        return props.config.rules[0]
    } else {
        return props.config.rules.find(rule => rule.identifier === _id)
    }
}
    
function addRule(event){
    let _add = {
        index: !event.calculatedLevel ? currentQuery.rules.length : 0,
        value: null,
        operator: selectRule(event.selectedRule)?.operators?.[0] || props.config?.ruleOperators?.[0].identifier || null,
        level: !event.calculatedLevel ? 0 : event.calculatedLevel,
        uuid: getUUID(),
        identifier: selectRule(event.selectedRule).identifier || props.config?.rules[0].identifier || null,
        ruleType: selectRule(event.selectedRule)
    }

    if(event.selectedRule === 'group'){
        let _groupElements = {
            isGroup: true,
            children: [],
        }
        _add = ({..._add, ..._groupElements})
    } 


    if(event.parentuuid){        
        findRule(currentQuery.rules, event.parentuuid).children.push(_add)
    } else  {
        currentQuery.rules.push(_add)
    }
    if(event.calculatedLevel >= currentQuery.levelOperators.length){
        addLevelOperator(event.calculatedLevel)
    }
}

function deleteRule(event){
    let _findItem = findRule(currentQuery.rules, event.parentuuid ? event.parentuuid : event.rule.uuid)
    if(event.parentuuid){
        _findItem.children.splice(_findItem.children.findIndex(item => item.uuid == event.rule.uuid), 1)
    } else {
        currentQuery.rules.splice(event.index, 1)
    }
    if(event.rule.isGroup){
        deleteLevelOperator(event.rule.level + 1)
    }
}

function addLevelOperator(level){
    let _level = {
        level: level,
    }
    let _findOperator = {...props.config.levelOperators[0], ..._level}
    currentQuery.levelOperators.push(_findOperator)
}

function deleteLevelOperator(level){
    currentQuery.levelOperators.splice(currentQuery.levelOperators.findIndex(operator => operator.level === level), 1)
}

function setLevelOperator(event){
    let _findOperator = props.config.levelOperators.find(operator => operator.identifier == event.currentRule.levelOperator)

    let _findCurrentOperator = currentQuery.levelOperators.find(operator => operator.level === event.calculatedLevel)
        _findCurrentOperator.identifier = _findOperator.identifier
        _findCurrentOperator.name = _findOperator.name
}

function updateRule(event){
    let _find = findRule(currentQuery.rules, event.currentRule.uuid)
    if(event._type === 'resetValue'){
        _find.value = null
    } else {
        _find.value = event.currentRule.value
    }

    _find.operator = event.currentRule.operator
    _find.index = event.props.index
    _find.level = event.props.calculatedLevel
    _find.identifier = event.currentRule.identifier
    _find.ruleType = event.currentRule.ruleType
}

</script>