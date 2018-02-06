import Axios from 'axios'
import Cookies from 'js-cookie'

const csrfToken = Cookies.get('csrftoken')
const requestHeader = {
  headers: {
    'X-CSRFToken': csrfToken
  }
}
const requestHeaderWithNullData = {
  headers: {
    'X-CSRFToken': csrfToken
  },
  data: {}
}

export default {
  get (url, request) {
    return Axios.get(url, request)
                .then((response) => Promise.resolve(response))
                .catch((error) => Promise.reject(error))
  },
  post (url, request) {
    return Axios.post(url, request, requestHeader)
                .then((response) => Promise.resolve(response))
                .catch((error) => Promise.reject(error))
  },
  patch (url, request) {
    return Axios.patch(url, request, requestHeader)
                .then((response) => Promise.resolve(response))
                .catch((error) => Promise.reject(error))
  },
  delete (url) {
    return Axios.delete(url, requestHeaderWithNullData)
                .then((response) => Promise.resolve(response))
                .catch((error) => Promise.reject(error))
  }
}
