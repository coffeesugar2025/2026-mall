export function getTextOptionList(): { label: string; value: string }[] {
  return [
    { label: "=", value: "=" },
    { label: "!=", value: "!=" },
    { label: ">", value: ">" },
    { label: "<", value: "<" },
    { label: ">=", value: ">=" },
    { label: "<=", value: "<=" }
  ];
}
 
export function getNumberOptionList(): { label: string; value: string }[] {
  return [
    { label: '等于', value: 'equal' },
    { label: '不等于', value: 'not_equal' },
    { label: '大于', value: 'greater_than' },
    { label: '小于', value: 'less_than' },
    { label: '大于等于', value: 'greater_than_or_equal' },
    { label: '小于等于', value: 'less_than_or_equal' },
    { label: '在范围内', value: 'between' },
  ];
}
 
export function getDateOptionList(): { label: string; value: string }[] {
  return [
    { label: '等于', value: 'equal' },
    { label: '不等于', value: 'not_equal' },
    { label: '在范围内', value: 'between' },
    { label: '早于', value: 'before' },
    { label: '晚于', value: 'after' },
  ];
}
export const exportSqlStringBySqlTree = (tree?: SQLNodeTree) => {
  if (!tree) {
    return ''
  }
  const { logic, compare, equal, word } = tree
  let compareList: string[] = []
  if (logic == 'or' || logic == 'and') {
    if (tree.children && tree.children.length) {
      compareList = tree.children?.map(treeNode => {
        return exportSqlStringBySqlTree(treeNode)
      })
    }
  } else if (logic == 'compare' && compare && word && equal) {
    let result = ''
    if (compare == 'IN') {
      if (Array.isArray(equal)) {
        result = `${word} ${compare} ${JSON.stringify(equal || [])}`
      } else {
        result = `${word} ${compare} ${equal}`
      }
    } else {
      switch (compare) {
        case 'NOT IN':
          if (Array.isArray(equal)) {
            result = `${word} ${compare} ${JSON.stringify(equal || [])}`
          } else {
            result = `${word} ${compare} ${equal}`
          }
          break
        case 'LIKE':
          result = `${word} LIKE '%${equal}%'`
          break
        // case 'end_match':
        //   result = `${word} LIKE %${equal}`
        //   break
        case 'NOT LIKE':
          result = `${word} NOT LIKE '%${equal}%'`
          break
        // case 'not_end_match':
        //   result = `${word} NOT LIKE %${equal}`
        //   break
        default:
          result = `${word} ${compare} ${equal}`
          break
      }
    }
    if (result) {
      compareList.push(result)
    }
  }
  compareList = compareList.filter(item => item)
  if (logic == 'or' || logic == 'and') {
    if (compareList.length > 1) {
      return `(${compareList.join(` ${logic} `)})`
    } else if (compareList.length == 1) {
      return compareList.toString()
    }
  } else if (logic == 'compare') {
    if (compareList.length) {
      return compareList.toString()
    }
  }
  return ''
}
 
export interface SQLNodeTree {
  logic: 'or' | 'and' | 'compare';
  compare?: string;
  word?: string;
  equal?: string | number | string[] | number[];
  children?: SQLNodeTree[];
}
 
// 定义 MapData 类型
export interface MapData {
  label: string;
  colName: string;
  value: string;
  type: 'STRING' | 'TIMESTAMP' | 'NUMBER';
}
 