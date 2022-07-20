var myModal = document.getElementById('#exampleModal')
var myInput = document.getElementById('#exampleInput')

myModal.addEventListener('shown.bs.modal', function () {
  myInput.focus()
})