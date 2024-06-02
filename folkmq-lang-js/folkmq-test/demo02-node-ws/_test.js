const {FolkMQ} = require("@noear/folkmq");

async function main() {
    const client = await FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
        .nameAs("demoapp")
        .connect();


    client.send(FolkMQ.newMessage("helloworld!").tag("hello"), "demoapp1").thenReply(resp=>{
        console.log(resp.dataAsString());
    }).thenError(err=>{
        console.log(err.message);
    });

}

main();