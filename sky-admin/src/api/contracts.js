const saveFields = {
  category: ['id', 'name', 'type', 'sort'],
  employee: ['id', 'username', 'name', 'phone', 'sex', 'idNumber'],
  dish: ['id', 'name', 'categoryId', 'price', 'image', 'description', 'status', 'flavors'],
  setmeal: ['id', 'name', 'categoryId', 'price', 'image', 'description', 'status', 'setmealDishes'],
}

/** Only send fields accepted by the matching Spring DTO. */
export function toSavePayload(kind, form) {
  if (!saveFields[kind]) throw new Error('未知的管理资源')
  return Object.fromEntries(saveFields[kind]
    .filter(key => form[key] !== undefined)
    .map(key => [key, form[key]]))
}
