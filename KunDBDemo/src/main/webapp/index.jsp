<html>
<body>
<h2>Hello World!</h2>
<button onclick="a()">ciehsi</button>
<script type="text/javascript">
function a(){
    alert("ce");
    $.ajax({
        url: "http://localhost:9999/hello",
        type: "get",
        dataType: "json",
        success: function (data) {
        	alert("getHbAccountInfo: "+data);
        },
        error : function(e) {
            alert(e.responseText);
         },
    })
}    
</script>
</body>
</html>
