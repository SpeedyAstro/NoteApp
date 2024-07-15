document.querySelector("#image-file-input").addEventListener("change", function(event) {
    let file = event.target.files[0];
    let reader = new FileReader();
    reader.onload = function (){
        document.querySelector("#upload-image-preview").src = reader.result;
    }
    reader.readAsDataURL(file);
});