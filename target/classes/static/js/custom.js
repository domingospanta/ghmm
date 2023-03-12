$(document).ready(function () {
  $("#btnClear").on("click", function (e) {
    e.preventDefault();
    $("#keyword").text("");
    window.location = $("#btnClear").data("url");
  });

  $().alert('close');

  /**
   * Sidebar toggle
   *
   */
  if (select('.toggle-sidebar-btn')) {
    on('click', '.toggle-sidebar-btn', function(e) {
      select('body').classList.toggle('toggle-sidebar')
    })
  }

  /**
   * Easy on scroll event listener
   */
  const onscroll = (el, listener) => {
    el.addEventListener('scroll', listener)
  }

  let selectHeader = select('#header')
  if (selectHeader) {
    const headerScrolled = () => {
      if (window.scrollY > 100) {
        selectHeader.classList.add('header-scrolled')
      } else {
        selectHeader.classList.remove('header-scrolled')
      }
    }
    window.addEventListener('load', headerScrolled)
    onscroll(document, headerScrolled)
  }
});

/**
 * Easy selector helper function
 */
const select = (el, all = false) => {
  el = el.trim()
  if (all) {
    return [...document.querySelectorAll(el)]
  } else {
    return document.querySelector(el)
  }
}

/**
 * Easy event listener function
 */
const on = (type, el, listener, all = false) => {
  if (all) {
    select(el, all).forEach(e => e.addEventListener(type, listener))
  } else {
    select(el, all).addEventListener(type, listener)
  }
}

function changePageSize() {
  $("#searchForm").submit();
}

function startMetricsGeneration() {
  $("#generationButtonSpinner").show();
  $("#generationButtonLabel").text("Loading...");

  metricsGenerationRequest();
}
var timer;
function metricsGenerationRequest() {
  $.ajax({
    url: "/metrics/generation/start",
    success:
        function (data) {
         console.log("data: " + data);
        },
    complete: function () {
      // Schedule the next request when the current one's complete
      timer = setInterval(metricsStatusRequest, 5000); // The interval set to 5 seconds
    }
  });
}

function metricsStatusRequest() {
  $.ajax({
    url: "/metrics/generation/status",
    success:
        function (processExecution) {
          console.log("status data:" + processExecution);
          if(!!processExecution && processExecution.running === false){
            $("#generationButton").attr('value', "Start");
            $("#generationButtonSpinner").hide();
            $("#generationButtonLabel").text("Start");
            clearInterval(timer);
            if(processExecution.error === true){
              $("#processResultDiv").removeClass("alert-success");
              $("#processResultDiv").addClass("alert-danger");
            } else {
              $("#processResultDiv").removeClass("alert-danger");
              $("#processResultDiv").addClass("alert-success");
            }
            $("#processResultMessage").text(processExecution.message);
            $("#processResultDiv").show();
          } else {
            $("#unprocessedMetrics").text(processExecution.processedItems + "/" + processExecution.totalItems);
            let bar = document.querySelector(".progress-bar");
            bar.style.width = ((processExecution.processedItems/processExecution.totalItems) * 100) + "%";
          }
        }
  });
}