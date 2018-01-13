<template>
  <div id="todo-list">
    <b-table striped hover
             @row-dblclicked="dblclicked"
             :items="todos"
             :fields="fields">
    </b-table>
  </div>
</template>

<script>
 import Todo from './Todo.vue'

 export default {
   name: 'todo-list',
   components: {
     Todo
   },
   computed: {
     todos () {
       return this.$store.state.todos
     }
   },
   methods: {
     dblclicked (item, index, event) {
       console.log('double clicked:', item.text)
     }
   },
   data () {
     return {
       fields: [
         {key: 'text', sortable: true},
         {key: 'deadline', sortable: true},
         {key: 'priority', sortable: true},
         {key: 'done', sortable: true},
         {key: 'memo', sortable: false}
       ]
     }
   }
 }
</script>
