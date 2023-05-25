<template>
  <v-data-table
      :headers="headers"
      :items="items"
      :search="search"
      :show-expand="expand"
      :sort-desc="true"
      class="table"
  >
    <template #top>
      <v-toolbar flat>
        <v-toolbar-title>
          {{ name }} {{ name === "" ? "" : "table" }}
        </v-toolbar-title>
        <v-spacer></v-spacer>
        <v-dialog>
          <template #activator="{ props }">
            <v-btn color="primary" dark v-bind="props">
              <v-icon>mdi-magnify</v-icon>
            </v-btn>
          </template>
          <v-text-field v-model="search" single-line hide-details/>
        </v-dialog>
        <v-dialog v-model="dialog" max-width="500px">
          <template #activator="{ props }">
            <v-btn color="primary" dark v-bind="props" v-show="!cannotCreate">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </template>

          <v-card>
            <form @submit.prevent="$emit('save')">
              <v-card-title>
                <span class="text-h5">{{ name }} details</span>
              </v-card-title>
              <v-card-text>
                <slot name="card-container"></slot>

              </v-card-text>

              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn color="blue darken-1" text @click="$emit('cancel')">
                  Cancel
                </v-btn>
                <v-btn
                    color="blue darken-1"
                    text
                    type="submit"
                >
                  Save
                </v-btn>
              </v-card-actions>
            </form>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>

    <template v-slot:item.actions="{ item }">
      <v-icon v-show="!cannotUpdate"
              size="small"
              class="me-2"
              @click="$emit('edit', item.raw)"
      >
        mdi-pencil
      </v-icon>
      <v-icon v-show="!cannotDelete && item.raw.roleByRoleId?.name !== 'admin'"
              size="small"
              @click="$emit('delete', item.raw)"
      >
        mdi-delete
      </v-icon>
    </template>

    <template v-slot:item.id="{item}">
      <v-checkbox v-model="selected" :value="item.raw.id"/>
    </template>

    <template v-slot:expanded-row="{ item, columns }">
      <slot name="expand" v-bind="{item: item.raw, width: columns.length}"></slot>
    </template>

    <template #no-data>
      <p>No data</p>
    </template>
  </v-data-table>
  <v-dialog
      width="auto"
  >
    <template v-slot:activator="{ props }">
      <v-snackbar v-model="snack" :color="color" :timeout="2000">
        <v-btn v-show="color === 'red'" icon="mdi-help-circle" variant="plain" v-bind="props"/>
        {{ snackText }}
      </v-snackbar>
    </template>
    <v-card>
      <v-card-text>
        <slot name="error"></slot>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    headers: Array,
    items: Array,
    schema: Object,
    name: {
      type: String,
      default: ""
    },
    expand: {
      type: Boolean,
      default: false,
    },
    cannotCreate: {
      type: Boolean,
      default: false,
    },
    cannotUpdate: {
      type: Boolean,
      default: false,
    },
    cannotDelete: {
      type: Boolean,
      default: false,
    },
  },

  emits: ['save', 'edit', 'delete', 'cancel'],
  inject: ['orders', 'setOrders', 'role'],

  data() {
    return {
      dialog: false,
      snack: false,
      snackText: "",
      search: '',
      color: '',
      selected: []
    };
  },

  //no differentiated logic needed since only used for menu
  watch: {
    selected: {
      handler(newVal) {
        this.setOrders(newVal)
      },
      deep: true
    }
  },

  mounted() {
    this.selected = this.orders
  }
};
</script>
<style>
.table {
  width: 1200px;
}
</style>
