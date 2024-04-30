const {FolkMQ} = require("@noear/folkmq");

async function main() {
    const client = await FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
        .nameAs("demoapp")
        .connect();

    client.transactionCheckback(m=>{
        console.info("check: " + m.getBodyAsString());
        m.acknowledge(true);
    })

    //订阅
    client.subscribe("test", null, true, message => {
        console.log(message.getBodyAsString());
    });

    //发布
    const tran = client.newTransaction();
    try {
        client.publish("test", FolkMQ.newMessage("hot-1").transaction(tran));
        client.publish("test", FolkMQ.newMessage("hot-2").transaction(tran));
        client.publish("test", FolkMQ.newMessage("hot-3").transaction(tran));

        tran.commit();
    } catch (e) {
        tran.rollback();
        console.error(e);
    }
}

main();