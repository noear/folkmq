import asyncio

from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqMessage import MqMessage


async def main():
    client = await (FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
                    .nameAs("demoapp")
                    .connect())

    client.send(MqMessage("helloworld!").tag("hello"), "demoapp1").then_reply(lambda resp:
        log.info(resp.dataAsString())
    ).then_error(lambda err:
        log.warning(err)
    )

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())

