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

function startMetricsGeneration(type) {
  $("#" + type + 'Spinner').show();
  $("#" + type + 'Label').text("Loading...");
  metricsGenerationRequest(type);
}
var timer;
function metricsGenerationRequest(type) {
  $.ajax({
    url: "/metrics/"+ type + "/start",
    success:
        function (result) {
         console.log("result: " + result);
         if(result === 'started'){
           timer = setInterval(metricsStatusRequest, 5000);
         } else {
           $("#" + type + 'Spinner').hide();
           $("#" + type + 'Label').text("Start");
           let processResultDiv = $("#" + type + "ProcessResultDiv");
           let processResultMessage = $("#" + type + "ProcessResultMessage");
           processResultDiv.removeClass("alert-success");
           processResultDiv.addClass("alert-danger");
           processResultMessage.text(result);
           processResultDiv.show();
         }
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
            let button = $("#" + processExecution.type + "Button");
            let spinner = $("#" + processExecution.type + "Spinner");
            let label =$("#" + processExecution.type + "Label");
            let processResultDiv = $("#" + processExecution.type + "ProcessResultDiv");
            let processResultMessage = $("#" + processExecution.type + "ProcessResultMessage");
            button.attr('value', "Start");
            spinner.hide();
            label.text("Start");
            clearInterval(timer);
            if(processExecution.error === true){
              processResultDiv.removeClass("alert-success");
              processResultDiv.addClass("alert-danger");
            } else {
              processResultDiv.removeClass("alert-danger");
              processResultDiv.addClass("alert-success");
            }
            processResultMessage.text(processExecution.message);
            processResultDiv.show();
          } else {
            let unprocessedMetrics = $("#" +  processExecution.type + "UnprocessedMetrics");
            let progressbar = $("#" +  processExecution.type + "ProgressBar");
            unprocessedMetrics.text(processExecution.processedItems + "/" + processExecution.totalItems);
            progressbar.width((processExecution.processedItems/processExecution.totalItems) * 100);
          }
        }
  });
}