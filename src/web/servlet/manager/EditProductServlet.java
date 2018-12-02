package task_itcaststore.web.servlet.manager;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import task_itcaststore.domain.Product;
import task_itcaststore.service.ProductService;
import task_itcaststore.utils.FileUploadUtils;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 后台系统
 * 用于编辑商品信息的servlet
 */
public class EditProductServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 创建javaBean,将上传数据封装.
		Product p = new Product();
		Map<String, String> map = new HashMap<String, String>();

		DiskFileItemFactory dfif = new DiskFileItemFactory();
		// 设置临时文件存储位置
		dfif.setRepository(new File(this.getServletContext().getRealPath(
				"/temp")));
		// 设置上传文件缓存大小为10m
		dfif.setSizeThreshold(1024 * 1024 * 10);
		// 创建上传组件
		ServletFileUpload upload = new ServletFileUpload(dfif);
		// 处理上传文件中文乱码
		upload.setHeaderEncoding("utf-8");
		try {
			// 解析request得到所有的FileItem
			List<FileItem> items = upload.parseRequest(request);
			// 遍历所有FileItem
			for (FileItem item : items) {
				// 判断当前是否是上传组件
				if (item.isFormField()) {
					// 不是上传组件
					String fieldName = item.getFieldName(); // 获取组件名称
					String value = item.getString("utf-8"); // 解决乱码问题
					map.put(fieldName, value);
				} else {
					// 是上传组件
					// 得到上传文件真实名称
					String fileName = item.getName();
					if (fileName != null && fileName.trim().length() > 0) {
						fileName = FileUploadUtils.getFileName(fileName);

						// 得到随机名称
						String randomName = FileUploadUtils
								.generateRandomFileName(fileName);

						// 得到随机目录
						String randomDir = FileUploadUtils
								.generateRandomDir(randomName);
						// 图片存储父目录
						String imgUrl_parent = "/productImg" + randomDir;

						File parentDir = new File(this.getServletContext()
								.getRealPath(imgUrl_parent));
						// 验证目录是否存在，如果不存在，创建出来
						if (!parentDir.exists()) {
							parentDir.mkdirs();
						}

						String imgUrl = imgUrl_parent + "/" + randomName;

						map.put("imgUrl", imgUrl);

						IOUtils.copy(item.getInputStream(),
								new FileOutputStream(new File(parentDir,
										randomName)));
						item.delete();
					}
				}

			}

		} catch (FileUploadException e) {
			e.printStackTrace();

		}

		try {
			// 将数据封装到javaBean中
			BeanUtils.populate(p, map);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}


		ProductService service = new ProductService();

		// 调用service完成修改商品操作
		service.editProduct(p);
		response.sendRedirect(request.getContextPath() + "/listProduct");
		return;

	}

}