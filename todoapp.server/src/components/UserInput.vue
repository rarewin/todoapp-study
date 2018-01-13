<template>
  <div id="user-inputs">
    <b-form inline @submit="createTodo">
      <b-row id="rowTodo">
        <b-col sm="4">
          <label class="sr-only" for="inputTodo">TODO</label>
          <b-input id="inputTodo"
                   v-model="newTodoText"
                   type="text"
                   @keydown.enter="createTodo"
                   required
                   placeholder="TODO Description">
          </b-input>
        </b-col>
        <b-col sm="2">
          <label class="sr-only" for="datepickerDeadline">Deadline</label>
          <datepicker v-model="newTodoDeadline"
                      id="datepickerDeadline"
                      placeholder="Deadline"
                      :clear-button="true">
          </datepicker>
        </b-col>
        <b-col sm="1">
          <label class="sr-only" for="selectPriority">Priority</label>
          <b-form-select id="selectPriority"
                         v-model="newTodoPriority"
                         :options="priorities">
            <option slot="first" :value="null">Priority</option>
          </b-form-select>
        </b-col>
        <b-col sm="4">
          <label class="sr-only" for="textTodoMemo">Memo</label>
          <b-form-textarea id="textTodoMemo"
                           style="width: 100%"
                           v-model="newTodoMemo"
                           placeholder="Memo for TODO"
                           :no-resize="true" :max-rows="1">
          </b-form-textarea>
        </b-col>
        <b-col sm="1">
          <b-button type="submit" variant="primary">Create</b-button>
        </b-col>
      </b-row>
    </b-form>
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
       newTodoMemo: '',
       priorities: {'1': '1 (High)', '2': '2', '3': '3', '4': '4', '5': '5 (Low)'}
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

<style>
 div#rowTodo {
   background: black;
 }

 .vdp-datepicker input,
 input#inputTodo,
 select#selectPriority,
 input#textTodoMemo {
   width: 100%;
   height: 100%;
   margin: 0 0 0 0;
 }
</style>
