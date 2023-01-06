$(document).ready(function () {
  $("#btnClear").on("click", function (e) {
    e.preventDefault();
    $("#keyword").text("");
    window.location = "/repo/examples/all";
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