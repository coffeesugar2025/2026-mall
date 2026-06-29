<template>
    <advanced-query-builder :config="config" v-model="currentQuery">
        <template #addRule>Add Rule</template>
        <template #addGroup>add a Group</template>
        <template #removeRules>Remove all Rules</template>
    </advanced-query-builder>
</template>

<script setup>
import { ref } from 'vue';
import advancedQueryBuilder from './components/query-builder.vue'
import selectInput from "./components/rules/select.vue"

const currentQuery = ref([])

const config = {
    levelOperators: [
        {
            name: 'and',
            identifier: 'AND'
        },
        {
            name: 'or',
            identifier: 'OR'
        },
    ],
    ruleOperators: [
        {
            name: 'contains',
            identifier: 'contain'
        },
        {
            name: 'contains not',
            identifier: 'notContain'
        },
        {
            name: 'is',
            identifier: 'is'
        },
        {
            name: 'is not',
            identifier: 'isNot'
        },
        {
            name: 'starts with',
            identifier: 'startsWith'
        },
        {
            name: 'ends with',
            identifier: 'endsWith'
        },
        {
            name: 'has no Data',
            identifier: 'isEmpty',
            hasInput: false,
        },
        {
            name: 'has Data',
            identifier: 'isNotEmpty',
            hasInput: false,
        },
    ],
    rules: [
        {
            name: 'Name',
            identifier: 'name',
            type: "text",
            icon: '#',
            initialValue: "",
            placeholder: "Placeholder",
        },
        {
            name: 'Housenumber',
            identifier: 'housenumber',
            type: 'number',
            icon: '©',
            initialValue: "",
            placeholder: 'Housenumber',
            operators: false
        },
        {
            name: 'Color',
            identifier: 'color',
            type: 'color',
            icon: '⁂',
            initialValue: "333",
            placeholder: 'Color',
            operators: ['is', 'isNot']
        },
        {
            name: "Selection",
            identifier: "txt",
            type: 'select',
            component: selectInput,
            initialValue: "",
            placeholder: "Placeholder",            
            operators: ['contains', 'notContain', 'isEmpty', 'isNotEmpty']
        }
    ]
}

</script>