$(document).ready(function () {
  $("#btnClear").on("click", function (e) {
    e.preventDefault();
    $("#keyword").text("");
    window.location = "/repo/examples/all";
  });
});

function changePageSize() {
  $("#searchForm").submit();
}