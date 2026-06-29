<template>
    <div class="qb-rule" :class="'qb-rule--' + rule.identifier + ' level-' + calculatedLevel">
        <div class="qb-rule-operator">
            <select v-if="showOperator"
                    v-model="currentRule.levelOperator" 
                    @change="$emit('level-operator-value', {currentRule, index, calculatedLevel})" 
                    class="form-select qb-select select">
                    <option v-for="operator in config.levelOperators"
                        :selected="operator.identifier == 'AND' ? 'selected' : ''"
                        :value="operator.identifier" :key="operator.identifier">
                        {{ operator.name }}    
                    </option>
            </select>
            <small v-if="!showOperator">    
                {{ levelOperators[calculatedLevel].name }}
            </small>
        </div>
        <div class="qb-rule-handle">        
        </div>
        <div class="qb-rule-container">
            <select v-model="currentRule.identifier"
                    class="qb-rule-name form-select qb-select select"
                    @change="ruleUpdate('resetValue')">
                    <option v-for="rule in config.rules"
                            :value="rule.identifier"
                            :key="rule.identifier">
                        <div v-if="rule.icon" class="qb-icon">
                            {{ rule.icon }}&nbsp;
                        </div>
                        <div class="qb-name">
                            {{ rule.name}}
                        </div>
                    </option>
            </select>
            <div v-if="hasRuleOperator" class="qb-rule-operators">
                <select v-model="currentRule.operator" 
                        class="form-select select qb-select" 
                        @change="ruleUpdate()">
                    <option v-for="operator in currentOperators" 
                            :value="operator.identifier" 
                            :key="operator.identifier">
                        {{ operator.name}}
                    </option>
                </select>
            </div>
            <div v-if="hasInput" class="qb-rule-input">
                <input  v-if="!currentRule.ruleType?.component" 
                        class="input-field form-control qb-input" 
                        v-model="currentRule.value" :type="currentRule.ruleType?.type" 
                        :placeholder="currentRule.ruleType?.placeholder"
                        @input="ruleUpdate()">
                <component  v-else :is="getComponent()" 
                            @emit-input="(event) => updateInput(event)"
                            :value="currentRule.value"
                            :placeholder="currentRule.ruleType.placeholder">
                </component>
            </div>        
            <div class="qb-rule-actions">
                <button type="button" 
                        @click="$emit('delete-rule', {rule, index, calculatedLevel, parentuuid})"
                        class="btn btn-secondary btn-icon qb-rule-actions--delete qb-btn">
                </button>
            </div>
        </div>
    </div>
</template>
<script setup>
import { computed, reactive, onMounted, watch } from 'vue'; 

defineOptions({
    name: 'query-item'
})

const props = defineProps({
    rule: {
        type: Object
    },
    index: {
        type: Number,
    },
    calculatedLevel: {
        type: Number
    },
    config:{
        type: Object,
    },
    parentIndex: {
        type: Number
    },
    parentuuid: {
        type: String
    },
    levelOperators: {
        type: Array
    }

})
const emit = defineEmits(['update-rule', 'delete-rule', 'level-operator-value', 'emit-input'])

const currentSelectedRule = computed (() => props.config.rules.find(rule => rule.identifier === props.rule.identifier))

const hasInput = computed(() => {
    let _findOperator = props.config.ruleOperators?.find(operator => operator.identifier == currentRule.operator)
    if(_findOperator?.hasInput == false){
        return !!_findOperator.hasInput
    } else {
        return true
    }
})

const hasRuleOperator = computed(() =>{
    if(!props.config.ruleOperators){
        return false
    } else {
        if(currentSelectedRule.value?.operators === false){
            return false
        } else {
            return true
        }
    }
})

const currentOperators = computed(() => {    
    if(currentSelectedRule.value?.operators){
        let _operators = []
        props.config.ruleOperators.forEach(operator => {
            currentSelectedRule.value.operators.forEach(ruleOperators => {
                if(operator.identifier === ruleOperators){
                    _operators.push(operator)
                }
            })
        })
        currentRule.operator = _operators[0]?.identifier
        return _operators
    } else {
        return props.config.ruleOperators
    }
})

watch(()=> props, (current, prev) => {
    ruleUpdate()
    if(current.index){
        currentRule.index = props.index
    }
    currentRule.value = props.rule.value
},{ deep: true });

const currentRule = reactive({
    levelOperator: 'AND',
    uuid: props.rule.uuid,
    value: null,
    operator: null,
    level: props.calculatedLevel,
    index: props.index,
    identifier: props.rule.identifier,
    ruleType: []
})

const showOperator = computed(() => {
    const   _isFirstofGroup = props.index == 0 && props.rule.isGroup == true,
            _isSecondofGroup = props.index == 0 && props.rule.level > 0,
            _isFirst = props.index == 0 && props.rule.level == 0,
            _isSecond = props.index == 1 && props.rule.level == 0;

    if(_isFirst){
        return false
    }
    if(_isSecondofGroup || _isSecond || _isFirstofGroup ){
        return true
    }
})

onMounted(() =>{
    currentRule.value = props.rule.value || props.rule.ruleType.initialValue || null
    currentRule.operator = props.rule.operator || props.rule.ruleType?.operators?.[0] || null
    currentRule.ruleType = props.rule.ruleType
    
    if(showOperator){
        currentRule.levelOperator = props.levelOperators[props.rule.level].identifier
    }

    if(props.parentuuid){
        currentRule.parentuuid = props.parentuuid
    }
    if(props.rule.isGroup){
        currentRule.index = props.index
    }
    ruleUpdate()
})

function ruleUpdate(_type){
    currentRule.ruleType = currentSelectedRule.value
    if(_type == 'resetValue'){
        currentRule.value = currentRule.ruleType.initialValue || null
    }

    emit('update-rule', {currentRule, props, _type}); 
}

function updateInput(event){
    currentRule.value = event[Object.keys(event)[0]]
    ruleUpdate()
}

function getComponent(){
    return currentSelectedRule.value.component
}
</script>
