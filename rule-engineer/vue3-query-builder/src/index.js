import queryBuilder from './components/query-builder.vue'
import nestedQuery from "./components/query-nested.vue"
import queryAdd from "./components/query-add.vue"
import queryItem from "./components/query-item.vue"

export default {
    install: (app, option) => {
        app.component("AdvancedQueryBuilder", queryBuilder)
        app.component("NestedQuery", nestedQuery)
        app.component("QueryAdd", queryAdd)
        app.component("QueryItem", queryItem)
    }
}

import './styles/index.scss'