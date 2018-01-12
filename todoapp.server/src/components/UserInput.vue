<template>
  <div id="user-inputs">
    <input v-model="newTodoText" v-on:keydown.enter="createTodo">
    <button v-on:click="clearTodos">
      Clear
    </button>
  </div>
</template>

<script>
 export default {
   name: 'user-inputs',
   data: function () {
     return { newTodoText: '' }
   },
   methods: {
     createTodo () {
       console.log(this.newTodoText, 'created!')
       this.$store.dispatch('addTodo', {text: this.newTodoText})
       this.newTodoText = ''
     },
     clearTodos () {
       this.$store.dispatch('clearTodos')
       console.log('Todos cleared!')
     }
   }
 }
</script>
