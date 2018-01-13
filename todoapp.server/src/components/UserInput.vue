<template>
  <div id="user-inputs">
    <input v-model="newTodoText"
           v-on:keydown.enter="createTodo">
    <datepicker v-model="newTodoDeadline"
                :clear-button="true">
    </datepicker>
    <div>
      <input type="radio" id="1" value="1" v-model="newTodoPriority">
      <label for="1">1</label>
      <input type="radio" id="2" value="2" v-model="newTodoPriority">
      <label for="2">2</label>
      <input type="radio" id="3" value="3" v-model="newTodoPriority">
      <label for="3">3</label>
      <input type="radio" id="4" value="4" v-model="newTodoPriority">
      <label for="4">4</label>
      <input type="radio" id="5" value="5" v-model="newTodoPriority">
      <label for="5">5</label>
    </div>
    <textarea v-model="newTodoMemo"></textarea>
  </div>
</template>

<script>
 import Datepicker from 'vuejs-datepicker'
 import moment from 'moment-strftime'

 export default {
   name: 'user-inputs',
   data: function () {
     return {
       newTodoText: '',
       newTodoDeadline: null,
       newTodoPriority: null,
       newTodoMemo: ''
     }
   },
   methods: {
     createTodo () {
       console.log(this.newTodoText, 'created!')
       this.$store.dispatch('addTodo', {
         text: this.newTodoText,
         deadline: (this.newTodoDeadline == null) ? null : moment(this.newTodoDeadline).strftime('%Y-%m-%d'),
         priority: this.newTodoPriority,
         done: false,
         memo: this.newTodoMemo
       })
       this.newTodoText = ''
       this.newTodoDeadline = null
       this.newTodoPriority = null
       this.newTodoMemo = ''
     },
     clearTodos () {
       this.$store.dispatch('clearTodos')
       console.log('Todos cleared!')
     }
   },
   components: {
     Datepicker
   }
 }
</script>
