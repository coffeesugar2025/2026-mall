<template>
    <div class="qb-actions__add-rule">        
        <select v-model="selectedRule" 
                @change="$emit('add-rule', {selectedRule, calculatedLevel, index, parentuuid}); resetValue()"
                class="select form-control qb-select">
                <option value="null" selected="selected" disabled>
                    <slot name="addRule">add Rule</slot>
                </option>
                <option v-for="(option, index) in config.rules" :key="index" :value="option.identifier">
                    <i>{{ option.icon }}</i> &nbsp;{{ option.name }}    
                </option>
                <option v-if="calculatedLevel < 2"
                        :value="'group'">
                    <slot name="addGroup"><i>+</i> add Group</slot>
                </option>
        </select>
    </div>

</template>
<script setup>
import { ref } from 'vue';

defineOptions({
    name: 'query-add'
})

defineEmits(['add-rule'])

const props = defineProps({
    config: {
        type: Object,
    },
    calculatedLevel: {
        type: Number
    },
    index: {
        type: Number,
    },
    parentuuid: {
        type: String
    }
})

const selectedRule = ref(null)

function resetValue(){
    selectedRule.value = null
}
</script>
