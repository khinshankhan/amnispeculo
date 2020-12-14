"use strict";

const ul = document.getElementById("status");
const input = document.querySelector('input[type="file"]');

input.addEventListener("change", async e => {
  ul.innerHTML = "";

  const fd = new FormData();
  const file = e.target.files[0];
  fd.append("file", file, file.name);

  const reader = new FileReader();
  reader.onload = async event => {
    const filecontents = window.btoa(event.target.result);
    updateStatus("http:localhost:9999/log?img=" + filecontents, file.name);
  };
  reader.onerror = error => reject(error);
  reader.readAsText(file);
});

const updateStatus = (link, linkname) => {
  const li = document.createElement("a");
  const a = document.createElement("a");
  a.setAttribute("href", link);
  a.innerHTML = linkname;
  li.appendChild(a);
  ul.appendChild(li);
};
