# vue3-advanced-query-builder

![](/docs/img/advancedquerybuilder.jpg)

## Install 

via npm
```
npm install vue3-advanced-query-builder --save
```

## Setup
register plugin globally via
```JS
import AdvancedQueryBuilder from "vue3-advanced-query-builder"
app.use(AdvancedQueryBuilder)
```
```html 
<style src="vue3-advanced-query-builder/dist/styles.css" />
```
The css shouldn't be imported globally. It could overwrite your basic css. Just put this directly into the component with the advanced query builder.

## Usage 

```HTML
<advanced-query-builder :config="config"
                        v-model="currentQuery"
></advanced-query-builder>
```

```JavaScript
const currentQuery = ref([])

const config = {
    levelOperators: [
        {
            name: 'and',
            identifier: 'AND'
        },
        {
            ...
        },
    ],
    ruleOperators: [
        {
            name: 'contains',
            identifier: 'contain',
            hasInput: false, // if ruleOperator is a Boolean as 'isEmpty'
        },
        {
            ...
        },
    ],

    rules: [
        {
            name: 'Name',
            identifier: 'name',
            type: "text", // input type - ignored, when custom input component
            component: componentName, // used to inlcude custom input field component
            icon: '#', // optional
            initialValue: "", // optional
            placeholder: "Placeholder", // optional
            operators: ['contain', ...], // shows only defined rule operators
            operators: false // if you don't want rule operators in this rule
        },
        {
            ...
        },
    ]

}
```

## Output

```json
{
    "levelOperators": [
        {
            "name": "or",
            "identifier": "OR",
            "level": 0
        },
        {
            "name": "and",
            "identifier": "AND",
            "level": 1
        }
    ],
    "rules": [
        {
            "index": 0,
            "value": "",
            "operator": "contain",
            "level": 0,
            "identifier": "name",
            "uuid": "7d9bd77c-4025-4b7e-ab59-b2e2d012d1a5",
            "ruleType": {
                "name": "Name",
                "identifier": "name",
                "type": "text",
                "icon": "#",
                "initialValue": "",
                "placeholder": "Placeholder"
            }
        },
        {
            "index": 1,
            "value": "",
            "operator": "contain",
            "level": 0,
            "identifier": "name",
            "uuid": "1b39b943-e9cc-412a-8c9d-e79625ac4421",
            "ruleType": {
                "name": "Name",
                "identifier": "name",
                "type": "text",
                "icon": "#",
                "initialValue": "",
                "placeholder": "Placeholder"
            },
            "isGroup": true,
            "children": [{
                "index": 0,
                "value": "",
                "operator": "contain",
                "level": 1,
                "identifier": "housenumber",
                "uuid": "857f1a30-a133-4293-99f0-7a35121926a0",
                "ruleType": {
                    "name": "Hausnummer",
                    "identifier": "housenumber",
                    "type": "Number",
                    "icon": "©",
                    "initialValue": "",
                    "placeholder": "Hausnummer"
                }
            }]
        }
    ]
}
```

## Slots

- **addRule** - "Add a new Rule" - Text
- **addGroup** - Text of "Add a new Input Group" Field 
- **removeRules** - Text of the "Remove all Rules" Button

## Input Components

```javascript

<select v-model="value" 
        @change="$emit('emit-input', {value})" 
        class="form-control qb-select select">
        <option value="null">Select Value</option>
        <option v-for="option in options"
            :value="option" :key="option.id">
            {{ option.name }}    
        </option>
</select>
```
```javascript
const value = ref(null)
const options = { 
            ...
    }
```

Every custom component gets the props ``value`` and ``placeholder``. You need to assign those is your component.

## changelog
[changelog](CHANGELOG.md)
