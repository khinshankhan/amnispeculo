"use strict";

const ul = document.getElementById("status");
const input = document.querySelector('input[type="file"]');

input.addEventListener("change", async e => {
  ul.innerHTML = "";

  const fd = new FormData();
  const file = e.target.files[0];
  fd.append(e.target.name, file, file.name);

  await getBase64(file, async base64Image => {
    const res = await fetch("http://localhost:9999/log?img=" + base64Image, {
      method: "GET"
    });
    console.log(res);
  });
});

const getBase64 = async (file, callback) => {
  const reader = new FileReader();
  reader.addEventListener("load", async () => await callback(reader.result));
  reader.readAsDataURL(file);
};
