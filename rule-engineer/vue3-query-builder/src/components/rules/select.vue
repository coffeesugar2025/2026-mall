<template>
    <select v-model="newValue" 
            @change="updateInput(newValue)"  
            class="form-control qb-select select">
            <option disabled value="null">{{ placeholder }}</option>
            <option v-for="option in options"
                :value="option" :key="option.id">
                {{ option.name }}    
            </option>
    </select>
    {{ value }}
</template>
<script setup>
import { shallowRef,  ref, computed, onMounted } from 'vue'

const props = defineProps({
    value:{
        type: Number
    },
    placeholder: {
        type: String
    }
})
const emit = defineEmits(['emit-input'])
function updateInput(event){
    let valueId = event.id
    emit('emit-input', {valueId})
}
const newValue = ref(null)
const options = [
    { 
        name: 'test 1',
        id: 1
    },
    { 
        name: 'test 2',
        id: 2
    },
    { 
        name: 'test 3',
        id: 3
    },
    { 
        name: 'test 4',
        id: 4
    },
    { 
        name: 'test 5',
        id: 5
    },
    { 
        name: 'test 6',
        id: 6
    },
    { 
        name: 'test 7',
        id: 7
    },
    { 
        name: 'test 8',
        id: 8
    },
]

onMounted(() => {
    if(props.value){
        newValue.value = options.find(x => x.id === props.value)
    }
})
</script>