<template>
  <div id="app" class="container">
    <user-input></user-input>
    <todo-list></todo-list>
  </div>
</template>

<script>
 import TodoList from './components/TodoList.vue'
 import UserInput from './components/UserInput.vue'

 export default {
   name: 'app',
   components: {
     TodoList,
     UserInput
   }
 }
</script>

<style>
 #app {
   font-family: 'Avenir', Helvetica, Arial, sans-serif;
   -webkit-font-smoothing: antialiased;
   -moz-osx-font-smoothing: grayscale;
   color: #2c3e50;
   margin-top: 60px;
 }
</style>
