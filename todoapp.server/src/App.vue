<template>
  <div id="app">
    <hello></hello>
    <user-input></user-input>
    <todo-list></todo-list>
  </div>
</template>

<script>
 import TodoList from './components/TodoList.vue'
 import UserInput from './components/UserInput.vue'
 import Hello from './components/Hello.vue'

 export default {
   name: 'app',
   components: {
     TodoList,
     UserInput,
     Hello
   }
 }
</script>
